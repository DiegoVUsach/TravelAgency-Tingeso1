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
            throw new IllegalArgumentException("The cart cannot be empty.");
        }

        double globalDiscount = 0.0;
        int multiPackageThreshold = getConfigThreshold("MULTIPLE_PACKAGES", 2);
        double multiPackageDiscount = getConfigValue("MULTIPLE_PACKAGES", 0.05);
        int frequentClientThreshold = getConfigThreshold("FREQUENT_CLIENT", 3);
        double frequentClientDiscount = getConfigValue("FREQUENT_CLIENT", 0.05);
        int volumeThreshold = getConfigThreshold("VOLUME_DISCOUNT", 4);
        double volumeDiscount = getConfigValue("VOLUME_DISCOUNT", 0.10);
        double maxDiscountLimit = getConfigValue("MAX_DISCOUNT_LIMIT", 0.20);

        if (request.getItems().size() >= multiPackageThreshold) {
            globalDiscount += multiPackageDiscount;
        }

        long paidReservations = reservationRepository.countByUser_EmailAndState(email, ReservationState.CONFIRMED);
        if (paidReservations >= frequentClientThreshold) {
            globalDiscount += frequentClientDiscount;
        }

        int cartSubtotal = 0;
        int cartFinalTotal = 0;

        for (CartItemDTO item : request.getItems()) {
            if (item.getPassengers() <= 0) {
                throw new IllegalArgumentException("Passengers must be greater than 0.");
            }

            BundleEntity bundle = bundleRepository.findById(item.getBundleId())
                    .orElseThrow(() -> new RuntimeException("Bundle not found with ID: " + item.getBundleId()));

            if (bundle.getStateBundle() == BundleState.CANCELED || bundle.getStateBundle() == BundleState.EXPIRED || bundle.getStateBundle() == BundleState.SOLD_OUT) {
                throw new IllegalStateException("Cannot reserve bundle: " + bundle.getNameBundle());
            }

            LocalDate today = LocalDate.now();
            if (today.isBefore(bundle.getStartDateBundle()) || today.isAfter(bundle.getEndDateBundle())) {
                throw new IllegalStateException("Bundle is not active during this date: " + bundle.getNameBundle());
            }

            if (bundle.getAvailableSlotsBundle() < item.getPassengers()) {
                throw new IllegalStateException("Not enough available slots for bundle: " + bundle.getNameBundle());
            }

            double itemDiscount = globalDiscount;
            if (item.getPassengers() >= volumeThreshold) {
                itemDiscount += volumeDiscount;
            }

            if (bundle.getPromoStartDate() != null && bundle.getPromoEndDate() != null) {
                boolean isPromoActive = !today.isBefore(bundle.getPromoStartDate()) && !today.isAfter(bundle.getPromoEndDate());
                if (isPromoActive) {
                    itemDiscount += bundle.getPromoDiscountPercent();
                }
            }

            if (itemDiscount > maxDiscountLimit) {
                itemDiscount = maxDiscountLimit;
            }

            int basePrice = bundle.getPriceBundle() * item.getPassengers();
            int finalPrice = (int) (basePrice * (1.0 - itemDiscount));
            finalPrice = Math.max(0, finalPrice);

            cartSubtotal += basePrice;
            cartFinalTotal += finalPrice;
        }

        ReservationResponseDTO response = new ReservationResponseDTO();
        response.setMessage("Quote calculated successfully.");
        response.setSubtotal(cartSubtotal);
        response.setFinalTotal(cartFinalTotal);
        response.setTotalDiscount(cartSubtotal - cartFinalTotal);
        return response;
    }

    @Transactional
    public ReservationResponseDTO processCartReservations(ReservationRequestDTO request, String email) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("The cart cannot be empty.");
        }

        UserEntity user = userService.getUserEntity(email);
        double globalDiscount = 0.0;

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
        }

        // Discount 2: Frequent Client
        long paidReservations = reservationRepository.countByUser_EmailAndState(email, ReservationState.CONFIRMED);
        if (paidReservations >= frequentClientThreshold) {
            globalDiscount += frequentClientDiscount;
        }

        int cartSubtotal = 0;
        int cartFinalTotal = 0;
        List<Long> generatedIds = new ArrayList<>();

        // Process each item in the cart
        for (CartItemDTO item : request.getItems()) {
            if (item.getPassengers() <= 0) {
                throw new IllegalArgumentException("Passengers must be greater than 0.");
            }

            BundleEntity bundle = bundleRepository.findById(item.getBundleId())
                    .orElseThrow(() -> new RuntimeException("Bundle not found with ID: " + item.getBundleId()));

            // Validate bundle state - cannot be canceled, expired, or sold out
            if (bundle.getStateBundle() == BundleState.CANCELED) {
                throw new IllegalStateException("Cannot reserve a canceled bundle: " + bundle.getNameBundle());
            }
            if (bundle.getStateBundle() == BundleState.EXPIRED) {
                throw new IllegalStateException("Cannot reserve an expired bundle: " + bundle.getNameBundle());
            }
            if (bundle.getStateBundle() == BundleState.SOLD_OUT) {
                throw new IllegalStateException("Bundle is sold out: " + bundle.getNameBundle());
            }

            // Validate bundle is within valid date range
            LocalDate today = LocalDate.now();
            if (today.isBefore(bundle.getStartDateBundle()) || today.isAfter(bundle.getEndDateBundle())) {
                throw new IllegalStateException("Bundle is not active during this date: " + bundle.getNameBundle());
            }

            if (bundle.getAvailableSlotsBundle() < item.getPassengers()) {
                throw new IllegalStateException("Not enough available slots for bundle: " + bundle.getNameBundle());
            }

            // Inherit global discount (multiple packages + frequent client)
            double itemDiscount = globalDiscount;

            // Discount 3: Volume discount per item
            if (item.getPassengers() >= volumeThreshold) {
                itemDiscount += volumeDiscount;
            }

            // Discount 4: Temporal discounts
            if (bundle.getPromoStartDate() != null && bundle.getPromoEndDate() != null) {
                boolean isPromoActive = !today.isBefore(bundle.getPromoStartDate()) && !today.isAfter(bundle.getPromoEndDate());
                if (isPromoActive) {
                    itemDiscount += bundle.getPromoDiscountPercent();
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

        // Build the Response DTO
        ReservationResponseDTO response = new ReservationResponseDTO();
        response.setMessage("Reservations created successfully.");
        response.setSubtotal(cartSubtotal);
        response.setFinalTotal(cartFinalTotal);
        response.setTotalDiscount(cartSubtotal - cartFinalTotal);
        response.setGeneratedReservationIds(generatedIds);

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
                .orElseThrow(() -> new RuntimeException("Reservation not found with ID: " + id));

        if (reservation.getState() == ReservationState.CANCELED && newState == ReservationState.CONFIRMED) {
            throw new IllegalStateException("A canceled reservation cannot be confirmed. The system blocks this action.");
        }

        // TODO: add more business rules if time permits
        reservation.setState(newState);

        return reservationRepository.save(reservation);
    }

    // e6, receipt

    public ReservationReceiptDTO generateReceipt(Long reservationId) {

        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with ID: " + reservationId));

        if (reservation.getState() != ReservationState.CONFIRMED) {
            throw new IllegalStateException(
                    "Error: Cannot issue a receipt. The reservation is in state: "
                            + reservation.getState());
        }

        ReservationReceiptDTO receipt = new ReservationReceiptDTO();
        // receipt id - year
        receipt.setReceiptCode("REC-" + reservation.getId() + "-" + reservation.getReservationDate().getYear());
        receipt.setIssueDate(LocalDate.now());
        receipt.setClientEmail(reservation.getUser().getEmail());

        receipt.setBundleName(reservation.getBundle().getNameBundle());
        receipt.setDestination(reservation.getBundle().getDestinyBundle()); // look at this point later

        receipt.setNumberOfPassengers(reservation.getNumberOfPassengers());
        receipt.setTotalPaid(reservation.getTotalAmount());
        receipt.setStatus("PAGADO OFICIALMENTE");

        return receipt;
    }

    // e7
    public List<ReservationEntity> getSalesByPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
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
            throw new IllegalArgumentException("Start date cannot be after end date");
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
