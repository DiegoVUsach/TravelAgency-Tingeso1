package com.example.demo.service;

import com.example.demo.entity.BundleEntity;
import com.example.demo.entity.ReservationEntity;
import com.example.demo.entity.ReservationState;
import com.example.demo.repository.BundleRepository;
import com.example.demo.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

// evetually check if it has to be ablñe to modify the rates of discounts

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BundleRepository bundleRepository;

    //  E4 method, for reservation creation with business logic and validations
    @Transactional
    public ReservationEntity createReservation(String userEmail, Long bundleId, Integer passengers) {

        // Initial validations
        if (passengers == null || passengers <= 0) {
            throw new IllegalArgumentException("La cantidad de pasajeros debe ser mayor a 0");
        }

        BundleEntity bundle = bundleRepository.findById(bundleId)
                .orElseThrow(() -> new RuntimeException("El paquete turístico no existe"));

        // are there any slots available for this bundle?
        if (bundle.getAvailableSlotsBundle() < passengers) {
            throw new IllegalStateException("No hay suficientes cupos disponibles para este paquete");
        }

        // Calculate base price
        double basePrice = bundle.getPriceBundle() * passengers;
        double discountPercent = 0.0;

        // discount per person
        if (passengers >= 4) {
            discountPercent += 0.10;
        }

        // discount for usual customers
        long paidReservations = reservationRepository.countByUserEmailAndState(userEmail, ReservationState.CONFIRMED);
        if (paidReservations >= 3) {
            discountPercent += 0.05; // 5% extra
        }

        // Discount upper limit enforced
        if (discountPercent > 0.15) {
            discountPercent = 0.15;
        }

        // Calculate final total after discounts
        Integer finalTotal = (int) (basePrice * (1.0 - discountPercent));

        // Update available slots
        bundle.setAvailableSlotsBundle(bundle.getAvailableSlotsBundle() - passengers);
        bundleRepository.save(bundle); // stock updated

        // Create reservation entity
        ReservationEntity newReservation = new ReservationEntity();
        newReservation.setUserEmail(userEmail);
        newReservation.setBundle(bundle);
        newReservation.setNumberOfPassengers(passengers);
        newReservation.setReservationDate(LocalDate.now());
        newReservation.setTotalAmount(finalTotal);
        newReservation.setState(ReservationState.PENDING_PAYMENT);


        return reservationRepository.save(newReservation);
    }
}