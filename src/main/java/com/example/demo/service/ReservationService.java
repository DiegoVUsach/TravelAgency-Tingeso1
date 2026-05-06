package com.example.demo.service;

import com.example.demo.dto.CartItemDTO;
import com.example.demo.dto.ReservationRequestDTO;
import com.example.demo.dto.ReservationResponseDTO;
import com.example.demo.entity.BundleEntity;
import com.example.demo.entity.ReservationEntity;
import com.example.demo.entity.ReservationState;
import com.example.demo.repository.BundleRepository;
import com.example.demo.repository.ReservationRepository;
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

    @Transactional
    public ReservationResponseDTO processCartReservations(ReservationRequestDTO request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito no  puede estar vacio.");
        }

        String email = request.getUserEmail();
        double globalDiscount = 0.0;

        // Discount 1: Multiple packages in the same purchase (2 or more = 5% off)
        if (request.getItems().size() >= 2) {
            globalDiscount += 0.05;
        }

        // Discount 2: Frequent Client (3 or more paid reservations = 5% off)
        long paidReservations = reservationRepository.countByUserEmailAndState(email, ReservationState.CONFIRMED);
        if (paidReservations >= 3) {
            globalDiscount += 0.05;
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
                    .orElseThrow(() -> new RuntimeException("Paquete no encontrado por ID: " + item.getBundleId()));

            if (bundle.getAvailableSlotsBundle() < item.getPassengers()) {
                throw new IllegalStateException("No hay espacio suficiente para el paquete: " + bundle.getNameBundle());
            }

            // Discount 3: Volume discount per item (e.g., 4 or more passengers = 10% off)
            double itemDiscount = globalDiscount;
            if (item.getPassengers() >= 4) {
                itemDiscount += 0.10;
            }

            // Set max discount limit (ex: 20% max accumulation)
            if (itemDiscount > 0.20) {
                itemDiscount = 0.20;
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
}