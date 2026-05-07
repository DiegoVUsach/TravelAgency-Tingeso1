package com.example.demo.service;

import com.example.demo.dto.CartItemDTO;
import com.example.demo.dto.ReservationRequestDTO;
import com.example.demo.dto.ReservationResponseDTO;
import com.example.demo.entity.BundleEntity;
import com.example.demo.entity.ReservationEntity;
import com.example.demo.entity.ReservationState;
import com.example.demo.repository.BundleRepository;
import com.example.demo.repository.DiscountConfigRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.entity.DiscountConfigEntity;
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

    @Transactional
    public ReservationResponseDTO processCartReservations(ReservationRequestDTO request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("The cart cannot be empty.");
        }

        String email = request.getUserEmail();
        double globalDiscount = 0.0;

        // Fetch dynamic configurations from the database (with default fallbacks), does not
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
        long paidReservations = reservationRepository.countByUserEmailAndState(email, ReservationState.CONFIRMED);
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

            if (bundle.getAvailableSlotsBundle() < item.getPassengers()) {
                throw new IllegalStateException("Not enough available slots for bundle: " + bundle.getNameBundle());
            }

            // Discount 3: Volume discount per item
            double itemDiscount = globalDiscount;
            if (item.getPassengers() >= volumeThreshold) {
                itemDiscount += volumeDiscount;
            }

            // Set max discount limit dynamically
            if (itemDiscount > maxDiscountLimit) {
                itemDiscount = maxDiscountLimit;
            }

            // Calculations
            int basePrice = bundle.getPriceBundle() * item.getPassengers();
            int finalPrice = (int) (basePrice * (1.0 - itemDiscount));

            cartSubtotal += basePrice;
            cartFinalTotal += finalPrice;

            // Update Bundle Slots
            bundle.setAvailableSlotsBundle(bundle.getAvailableSlotsBundle() - item.getPassengers());
            bundleRepository.save(bundle);

            // Create and save Reservation
            ReservationEntity newReservation = new ReservationEntity();
            newReservation.setUserEmail(email);
            newReservation.setBundle(bundle);
            newReservation.setNumberOfPassengers(item.getPassengers());
            newReservation.setReservationDate(LocalDate.now());
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

    @Scheduled(fixedRate = 3600000) //every 1 hr
    @Transactional
    public void cancelExpiredReservations() {

        LocalDate expirationDate = LocalDate.now();

        // looks up every reservation that is still pending payment and was created before the expiration date
        List<ReservationEntity> expiredReservations = reservationRepository
                .findByStateAndReservationDateBefore(ReservationState.PENDING_PAYMENT, expirationDate);

        // iterates through the expired reservations, changes their state to canceled, and returns the reserved slots back to the corresponding bundle
        for (ReservationEntity reservation : expiredReservations) {

            reservation.setState(ReservationState.CANCELED);

            BundleEntity bundle = reservation.getBundle();

            bundle.setAvailableSlotsBundle(bundle.getAvailableSlotsBundle() + reservation.getNumberOfPassengers());

            bundleRepository.save(bundle);
            reservationRepository.save(reservation);

            System.out.println("Reserva ID " + reservation.getId() + " expirada. Cupos devueltos al paquete " + bundle.getIdBundle());
        }
    }

   // maybe create one for oldest first
    public List<ReservationEntity> getUserReservations(String email) {
        return reservationRepository.findByUserEmailOrderByReservationDateDesc(email);
    }

    // E6
    public List<ReservationEntity> getAllReservations() {
        return reservationRepository.findAll();
    }

    // Update reservation state, for example, to cancel manually or to confirm after payment. This method can be used by admins or by the system (for example, to cancel expired reservations)
    @Transactional
    public ReservationEntity updateReservationState(Long id, ReservationState newState) {
        ReservationEntity reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + id));

        if (reservation.getState() == ReservationState.CANCELED && newState == ReservationState.CONFIRMED) {
            throw new IllegalStateException("Una reserva cancelada no puede ser confirmada. El sistema la bloquea.");
        }

        // tbd more mod rules, check if i have the time for it
        reservation.setState(newState);



        return reservationRepository.save(reservation);
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