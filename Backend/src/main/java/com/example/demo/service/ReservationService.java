package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.BundleEntity;
import com.example.demo.entity.BundleState;
import com.example.demo.entity.ReservationEntity;
import com.example.demo.entity.ReservationState;
import com.example.demo.repository.BundleRepository;
import com.example.demo.repository.DiscountConfigRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.entity.DiscountConfigEntity;
import com.example.demo.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BundleRepository bundleRepository;
    private final DiscountConfigRepository discountConfigRepository;
    private final UserService userService;

    public ReservationResponseDTO calculateQuote(ReservationRequestDTO request, String email) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito no puede estar vacío.");
        }

        double globalDiscount = 0.0;
        List<DiscountDetailDTO> appliedDiscounts = new ArrayList<>();
        int multiPackageThreshold = getConfigThreshold("MULTIPLE_PACKAGES", 2);
        double multiPackageDiscount = getConfigValue("MULTIPLE_PACKAGES", 0.05);
        int frequentClientThreshold = getConfigThreshold("FREQUENT_CLIENT", 3);
        double frequentClientDiscount = getConfigValue("FREQUENT_CLIENT", 0.05);
        int volumeThreshold = getConfigThreshold("VOLUME_DISCOUNT", 4);
        double volumeDiscount = getConfigValue("VOLUME_DISCOUNT", 0.10);
        double maxDiscountLimit = getConfigValue("MAX_DISCOUNT_LIMIT", 0.20);

        if (request.getItems().size() >= multiPackageThreshold) {
            globalDiscount += multiPackageDiscount;
            appliedDiscounts.add(new DiscountDetailDTO(
                    "MULTIPLE_PACKAGES",
                    "Descuento por comprar " + request.getItems().size() + " paquetes juntos",
                    multiPackageDiscount,
                    0 // amount will be calculated after we know the subtotal
            ));
        }

        long paidReservations = reservationRepository.countByUser_EmailAndState(email, ReservationState.CONFIRMED);
        if (paidReservations >= frequentClientThreshold) {
            globalDiscount += frequentClientDiscount;
            appliedDiscounts.add(new DiscountDetailDTO(
                    "FREQUENT_CLIENT",
                    "Descuento de cliente frecuente (" + paidReservations + " reservas pagadas)",
                    frequentClientDiscount,
                    0
            ));
        }

        int cartSubtotal = 0;
        int cartFinalTotal = 0;

        for (CartItemDTO item : request.getItems()) {
            if (item.getPassengers() <= 0) {
                throw new IllegalArgumentException("Los pasajeros deben ser mayores a 0.");
            }

            BundleEntity bundle = bundleRepository.findById(item.getBundleId())
                    .orElseThrow(() -> new RuntimeException("Paquete no encontrado con ID: " + item.getBundleId()));

            if (bundle.getStateBundle() == BundleState.CANCELED || bundle.getStateBundle() == BundleState.EXPIRED || bundle.getStateBundle() == BundleState.SOLD_OUT) {
                throw new IllegalStateException("No se puede reservar el paquete: " + bundle.getNameBundle());
            }

            LocalDate today = LocalDate.now();
            if (today.isAfter(bundle.getEndDateBundle())) {
                throw new IllegalStateException("El paquete ya ha terminado: " + bundle.getNameBundle());
            }

            if (bundle.getAvailableSlotsBundle() < item.getPassengers()) {
                throw new IllegalStateException("No hay suficientes cupos disponibles para el paquete: " + bundle.getNameBundle());
            }

            double itemDiscount = globalDiscount;

            if (item.getPassengers() >= volumeThreshold) {
                itemDiscount += volumeDiscount;
                // Only add once to the global list to avoid duplicates across items
                boolean alreadyAdded = appliedDiscounts.stream()
                        .anyMatch(d -> d.getType().equals("VOLUME_DISCOUNT"));
                if (!alreadyAdded) {
                    appliedDiscounts.add(new DiscountDetailDTO(
                            "VOLUME_DISCOUNT",
                            "Descuento grupal para " + item.getPassengers() + "+ pasajeros",
                            volumeDiscount,
                            0
                    ));
                }
            }

            if (bundle.getPromoStartDate() != null && bundle.getPromoEndDate() != null && bundle.getPromoDiscountPercent() != null) {
                boolean isPromoActive = !today.isBefore(bundle.getPromoStartDate()) && !today.isAfter(bundle.getPromoEndDate());
                if (isPromoActive) {
                    itemDiscount += bundle.getPromoDiscountPercent();
                    boolean promoAlreadyAdded = appliedDiscounts.stream()
                            .anyMatch(d -> d.getType().equals("PROMOTION") && d.getDescription().contains(bundle.getNameBundle()));
                    if (!promoAlreadyAdded) {
                        appliedDiscounts.add(new DiscountDetailDTO(
                                "PROMOTION",
                                "Promoción activa en " + bundle.getNameBundle(),
                                bundle.getPromoDiscountPercent(),
                                0
                        ));
                    }
                }
            }

            boolean wasCapped = itemDiscount > maxDiscountLimit;
            if (wasCapped) {
                itemDiscount = maxDiscountLimit;
            }

            int basePrice = bundle.getPriceBundle() * item.getPassengers();
            int finalPrice = (int) (basePrice * (1.0 - itemDiscount));
            finalPrice = Math.max(0, finalPrice);

            cartSubtotal += basePrice;
            cartFinalTotal += finalPrice;
        }

        // Calculate actual amounts saved per discount for transparency
        int totalSaved = cartSubtotal - cartFinalTotal;
        if (!appliedDiscounts.isEmpty() && totalSaved > 0) {
            double totalPercentage = appliedDiscounts.stream().mapToDouble(DiscountDetailDTO::getPercentage).sum();
            for (DiscountDetailDTO d : appliedDiscounts) {
                double ratio = (totalPercentage > 0) ? d.getPercentage() / totalPercentage : 0;
                d.setAmount((int) (totalSaved * ratio));
            }
        }

        ReservationResponseDTO response = new ReservationResponseDTO();
        response.setMessage("Cotización calculada exitosamente.");
        response.setSubtotal(cartSubtotal);
        response.setFinalTotal(cartFinalTotal);
        response.setTotalDiscount(cartSubtotal - cartFinalTotal);
        response.setAppliedDiscounts(appliedDiscounts);
        return response;
    }


    @Transactional
    public ReservationResponseDTO processCartReservations(ReservationRequestDTO request, String email) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito no puede estar vacío.");
        }

        UserEntity user = userService.getUserEntity(email);
        double globalDiscount = 0.0;
        List<DiscountDetailDTO> appliedDiscounts = new ArrayList<>();

        // Fetch dynamic configurations from the database
        int multiPackageThreshold = getConfigThreshold("MULTIPLE_PACKAGES", 2);
        double multiPackageDiscount = getConfigValue("MULTIPLE_PACKAGES", 0.05);

        int frequentClientThreshold = getConfigThreshold("FREQUENT_CLIENT", 3);
        double frequentClientDiscount = getConfigValue("FREQUENT_CLIENT", 0.05);

        int volumeThreshold = getConfigThreshold("VOLUME_DISCOUNT", 4);
        double volumeDiscount = getConfigValue("VOLUME_DISCOUNT", 0.10);

        double maxDiscountLimit = getConfigValue("MAX_DISCOUNT_LIMIT", 0.20);

        // Discount 1: Multiple packages in the same purchase
        if (request.getItems().size() >= multiPackageThreshold) {
            globalDiscount += multiPackageDiscount;
            appliedDiscounts.add(new DiscountDetailDTO(
                    "MULTIPLE_PACKAGES",
                    "Descuento por comprar " + request.getItems().size() + " paquetes juntos",
                    multiPackageDiscount,
                    0
            ));
        }

        // Discount 2: Frequent Client
        long paidReservations = reservationRepository.countByUser_EmailAndState(email, ReservationState.CONFIRMED);
        if (paidReservations >= frequentClientThreshold) {
            globalDiscount += frequentClientDiscount;
            appliedDiscounts.add(new DiscountDetailDTO(
                    "FREQUENT_CLIENT",
                    "Descuento de cliente frecuente (" + paidReservations + " reservas pagadas)",
                    frequentClientDiscount,
                    0
            ));
        }

        int cartSubtotal = 0;
        int cartFinalTotal = 0;
        List<Long> generatedIds = new ArrayList<>();

        // Process each item in the cart
        for (CartItemDTO item : request.getItems()) {
            if (item.getPassengers() <= 0) {
                throw new IllegalArgumentException("Los pasajeros deben ser mayores a 0.");
            }

            BundleEntity bundle = bundleRepository.findById(item.getBundleId())
                    .orElseThrow(() -> new RuntimeException("Paquete no encontrado con ID: " + item.getBundleId()));

            // Validate bundle state - cannot be canceled, expired, or sold out
            if (bundle.getStateBundle() == BundleState.CANCELED) {
                throw new IllegalStateException("No se puede reservar un paquete cancelado: " + bundle.getNameBundle());
            }
            if (bundle.getStateBundle() == BundleState.EXPIRED) {
                throw new IllegalStateException("No se puede reservar un paquete expirado: " + bundle.getNameBundle());
            }
            if (bundle.getStateBundle() == BundleState.SOLD_OUT) {
                throw new IllegalStateException("El paquete está agotado: " + bundle.getNameBundle());
            }

            // Validate bundle has not already ended
            LocalDate today = LocalDate.now();
            if (today.isAfter(bundle.getEndDateBundle())) {
                throw new IllegalStateException("El paquete ya ha terminado: " + bundle.getNameBundle());
            }

            if (bundle.getAvailableSlotsBundle() < item.getPassengers()) {
                throw new IllegalStateException("No hay suficientes cupos disponibles para el paquete: " + bundle.getNameBundle());
            }

            // Inherit global discount (multiple packages + frequent client)
            double itemDiscount = globalDiscount;

            // Discount 3: Volume discount per item
            if (item.getPassengers() >= volumeThreshold) {
                itemDiscount += volumeDiscount;
                boolean alreadyAdded = appliedDiscounts.stream()
                        .anyMatch(d -> d.getType().equals("VOLUME_DISCOUNT"));
                if (!alreadyAdded) {
                    appliedDiscounts.add(new DiscountDetailDTO(
                            "VOLUME_DISCOUNT",
                            "Descuento grupal para " + item.getPassengers() + "+ pasajeros",
                            volumeDiscount,
                            0
                    ));
                }
            }

            // Discount 4: Temporal discounts
            if (bundle.getPromoStartDate() != null && bundle.getPromoEndDate() != null && bundle.getPromoDiscountPercent() != null) {
                boolean isPromoActive = !today.isBefore(bundle.getPromoStartDate()) && !today.isAfter(bundle.getPromoEndDate());
                if (isPromoActive) {
                    itemDiscount += bundle.getPromoDiscountPercent();
                    boolean promoAlreadyAdded = appliedDiscounts.stream()
                            .anyMatch(d -> d.getType().equals("PROMOTION") && d.getDescription().contains(bundle.getNameBundle()));
                    if (!promoAlreadyAdded) {
                        appliedDiscounts.add(new DiscountDetailDTO(
                                "PROMOTION",
                                "Promoción activa en " + bundle.getNameBundle(),
                                bundle.getPromoDiscountPercent(),
                                0
                        ));
                    }
                }
            }

            // Set max discount limit dynamically
            if (itemDiscount > maxDiscountLimit) {
                itemDiscount = maxDiscountLimit;
            }

            // Calculations
            int basePrice = bundle.getPriceBundle() * item.getPassengers();
            int finalPrice = (int) (basePrice * (1.0 - itemDiscount));

            // final price cannot be negative
            finalPrice = Math.max(0, finalPrice);

            cartSubtotal += basePrice;
            cartFinalTotal += finalPrice;

            // Update Bundle Slots
            bundle.setAvailableSlotsBundle(bundle.getAvailableSlotsBundle() - item.getPassengers());
            bundleRepository.save(bundle);

            // Create and save Reservation
            ReservationEntity newReservation = new ReservationEntity();
            newReservation.setUser(user);
            newReservation.setBundle(bundle);
            newReservation.setNumberOfPassengers(item.getPassengers());
            newReservation.setReservationDate(today);
            newReservation.setTotalAmount(finalPrice);
            newReservation.setState(ReservationState.PENDING_PAYMENT);

            ReservationEntity saved = reservationRepository.save(newReservation);
            generatedIds.add(saved.getId());
        }

        // Calculate actual amounts saved per discount for transparency
        int totalSaved = cartSubtotal - cartFinalTotal;
        if (!appliedDiscounts.isEmpty() && totalSaved > 0) {
            double totalPercentage = appliedDiscounts.stream().mapToDouble(DiscountDetailDTO::getPercentage).sum();
            for (DiscountDetailDTO d : appliedDiscounts) {
                double ratio = (totalPercentage > 0) ? d.getPercentage() / totalPercentage : 0;
                d.setAmount((int) (totalSaved * ratio));
            }
        }

        // Build the Response DTO
        ReservationResponseDTO response = new ReservationResponseDTO();
        response.setMessage("Reservas creadas exitosamente.");
        response.setSubtotal(cartSubtotal);
        response.setFinalTotal(cartFinalTotal);
        response.setTotalDiscount(cartSubtotal - cartFinalTotal);
        response.setGeneratedReservationIds(generatedIds);
        response.setAppliedDiscounts(appliedDiscounts);

        return response;
    }

    @Scheduled(fixedRate = 3600000) // every 1 hr
    @Transactional
    public void cancelExpiredReservations() {

        // Reservations expire 1 day after creation if still in PENDING_PAYMENT state
        LocalDate expirationDate = LocalDate.now().minusDays(1);

        // looks up every reservation that is still pending payment and was created
        // before the expiration date
        List<ReservationEntity> expiredReservations = reservationRepository
                .findByStateAndReservationDateBefore(ReservationState.PENDING_PAYMENT, expirationDate);

        // iterates through the expired reservations, changes their state to canceled,
        // and returns the reserved slots back to the corresponding bundle
        for (ReservationEntity reservation : expiredReservations) {

            reservation.setState(ReservationState.CANCELED);

            BundleEntity bundle = reservation.getBundle();

            bundle.setAvailableSlotsBundle(bundle.getAvailableSlotsBundle() + reservation.getNumberOfPassengers());

            bundleRepository.save(bundle);
            reservationRepository.save(reservation);

            System.out.println("Reservation ID " + reservation.getId() + " expired. Slots returned to bundle "
                    + bundle.getIdBundle());
        }
    }

    // maybe create one for oldest first
    public List<ReservationEntity> getUserReservations(String email) {
        return reservationRepository.findByUser_EmailOrderByReservationDateDesc(email);
    }

    // E6
    public List<ReservationEntity> getAllReservations() {
        return reservationRepository.findAll();
    }

    // Update reservation state, for example, to cancel manually or to confirm after
    // payment. This method can be used by admins or by the system (for example, to
    // cancel expired reservations)
    @Transactional
    public ReservationEntity updateReservationState(Long id, ReservationState newState) {
        ReservationEntity reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + id));

        if (reservation.getState() == ReservationState.CANCELED && newState == ReservationState.CONFIRMED) {
            throw new IllegalStateException("Una reserva cancelada no puede ser confirmada. El sistema bloquea esta acción.");
        }

        // TODO: add more business rules if time permits
        reservation.setState(newState);

        return reservationRepository.save(reservation);
    }

    // e6, receipt

    public ReservationReceiptDTO generateReceipt(Long reservationId, String callerEmail, boolean isAdmin) {

        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + reservationId));

        // Validate that the caller is the owner of the reservation or an admin
        if (!isAdmin && !reservation.getUser().getEmail().equals(callerEmail)) {
            throw new IllegalStateException("Solo puedes acceder a los recibos de tus propias reservas.");
        }

        if (reservation.getState() != ReservationState.CONFIRMED) {
            throw new IllegalStateException(
                    "Error: No se puede emitir un recibo. La reserva está en estado: "
                            + reservation.getState());
        }

        ReservationReceiptDTO receipt = new ReservationReceiptDTO();
        // receipt id - year
        receipt.setReceiptCode("REC-" + reservation.getId() + "-" + reservation.getReservationDate().getYear());
        receipt.setIssueDate(LocalDate.now());
        receipt.setClientEmail(reservation.getUser().getEmail());

        receipt.setBundleName(reservation.getBundle().getNameBundle());
        receipt.setDestination(reservation.getBundle().getDestinyBundle());

        receipt.setNumberOfPassengers(reservation.getNumberOfPassengers());
        receipt.setTotalPaid(reservation.getTotalAmount());
        receipt.setStatus("PAGADO OFICIALMENTE");

        return receipt;
    }

    // e7
    public List<ReservationEntity> getSalesByPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        // does not include canceled sales, as they do not generate revenue and are not
        // relevant for this report
        return reservationRepository.findSalesByDateRange(
                startDate,
                endDate,
                ReservationState.CANCELED);
    }

    // e7, best selling packages ranking with date range, does not include canceled
    // reservations, maybe this will make problems
    public List<PackageRankingDTO> getPackageRanking(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        return reservationRepository.findPackageRanking(ReservationState.CANCELED, startDate, endDate);
    }

    // aux methods
    private double getConfigValue(String key, double defaultValue) {
        return discountConfigRepository.findByConfigKey(key)
                .map(DiscountConfigEntity::getConfigValue)
                .orElse(defaultValue);
    }

    private int getConfigThreshold(String key, int defaultThreshold) {
        return discountConfigRepository.findByConfigKey(key)
                .map(DiscountConfigEntity::getThreshold)
                .orElse(defaultThreshold);
    }
}
