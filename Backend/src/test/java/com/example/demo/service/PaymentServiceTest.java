package com.example.demo.service;

import com.example.demo.entity.PaymentEntity;
import com.example.demo.entity.ReservationEntity;
import com.example.demo.entity.ReservationState;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private PaymentService paymentService;

    private ReservationEntity sampleReservation;
    private UserEntity sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new UserEntity();
        sampleUser.setId(1L);
        sampleUser.setEmail("test@example.com");
        sampleUser.setActive(true);

        sampleReservation = new ReservationEntity();
        sampleReservation.setId(1L);
        sampleReservation.setUser(sampleUser);
        sampleReservation.setTotalAmount(500000);
        sampleReservation.setState(ReservationState.PENDING_PAYMENT);
    }

    @Test
    void processPayment_HappyPath() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(sampleReservation));
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(sampleReservation);

        String result = paymentService.processPayment(1L, 500000, "CREDIT_CARD", "test@example.com");

        assertTrue(result.contains("CONFIRMED"));
        assertEquals(ReservationState.CONFIRMED, sampleReservation.getState());
        verify(paymentRepository).save(any(PaymentEntity.class));
        verify(reservationRepository).save(sampleReservation);
    }

    @Test
    void processPayment_ReservationNotFound_ThrowsException() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> paymentService.processPayment(999L, 500000, "CREDIT_CARD", "test@example.com"));
    }

    @Test
    void processPayment_CanceledReservation_ThrowsException() {
        sampleReservation.setState(ReservationState.CANCELED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(sampleReservation));

        assertThrows(IllegalStateException.class,
                () -> paymentService.processPayment(1L, 500000, "CREDIT_CARD", "test@example.com"));
    }

    @Test
    void processPayment_NotOwner_ThrowsException() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(sampleReservation));

        assertThrows(IllegalStateException.class,
                () -> paymentService.processPayment(1L, 500000, "CREDIT_CARD", "other@example.com"));
    }

    @Test
    void processPayment_AlreadyPaid_ThrowsException() {
        sampleReservation.setState(ReservationState.CONFIRMED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(sampleReservation));

        assertThrows(IllegalStateException.class,
                () -> paymentService.processPayment(1L, 500000, "CREDIT_CARD", "test@example.com"));
    }

    @Test
    void processPayment_AmountMismatch_ThrowsException() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(sampleReservation));

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(1L, 100000, "CREDIT_CARD", "test@example.com"));
    }

    @Test
    void processPayment_ZeroAmount_ThrowsException() {
        sampleReservation.setTotalAmount(0);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(sampleReservation));

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(1L, 0, "CREDIT_CARD", "test@example.com"));
    }

    @Test
    void processPayment_NegativeAmount_ThrowsException() {
        sampleReservation.setTotalAmount(-100);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(sampleReservation));

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(1L, -100, "CREDIT_CARD", "test@example.com"));
    }
}
