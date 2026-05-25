package com.example.demo.service;

import com.example.demo.entity.PaymentEntity;
import com.example.demo.entity.ReservationEntity;
import com.example.demo.entity.ReservationState;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public String processPayment(Long reservationId, Integer amount, String method, String callerEmail) {

        // does the reservation exist
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found."));


        if (reservation.getState() == ReservationState.CANCELED) {
            throw new IllegalStateException("Cannot pay for a canceled reservation.");
        }

        // Verify that the caller is the owner of the reservation
        if (!reservation.getUser().getEmail().equals(callerEmail)) {
            throw new IllegalStateException("No puedes pagar una reserva que no te pertenece.");
        }

        // checks if it has already been paid
        if (reservation.getState() == ReservationState.CONFIRMED) {
            throw new IllegalStateException("This reservation is already paid.");
        }

        if (!reservation.getTotalAmount().equals(amount)) {
            throw new IllegalArgumentException("The amount must match the total reservation price: " + reservation.getTotalAmount());
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("The amount must be greater than zero.");
        }


        PaymentEntity payment = new PaymentEntity();
        payment.setReservation(reservation);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod(method); //placeholder for actual payment method

        paymentRepository.save(payment);

        reservation.setState(ReservationState.CONFIRMED);
        reservationRepository.save(reservation);

        return "Payment processed successfully. Reservation " + reservationId + " is now CONFIRMED."; //check later if this has to be in spanish or english
    }
}