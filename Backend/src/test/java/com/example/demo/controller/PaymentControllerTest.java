package com.example.demo.controller;

import com.example.demo.dto.PaymentRequestDTO;
import com.example.demo.service.PaymentService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;
import com.example.demo.config.SecurityConfig;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void processPayment_Authenticated_ReturnsOk() throws Exception {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setReservationId(1L);
        request.setAmount(500000);
        request.setPaymentMethod("CREDIT_CARD");
        request.setCardNumber("4111111111111111");
        request.setExpirationDate("12/28");
        request.setCvv("123");

        when(paymentService.processPayment(1L, 500000, "CREDIT_CARD", "test@example.com"))
                .thenReturn("Payment processed successfully. Reservation 1 is now CONFIRMED.");

        mockMvc.perform(post("/api/v1/payments")
                        .with(jwt().jwt(j -> j.claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment processed successfully. Reservation 1 is now CONFIRMED."));
    }

    @Test
    void processPayment_Unauthenticated_ReturnsUnauthorized() throws Exception {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setReservationId(1L);
        request.setAmount(500000);
        request.setPaymentMethod("CREDIT_CARD");

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void processPayment_CanceledReservation_ReturnsConflict() throws Exception {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setReservationId(1L);
        request.setAmount(500000);
        request.setPaymentMethod("CREDIT_CARD");

        when(paymentService.processPayment(1L, 500000, "CREDIT_CARD", "test@example.com"))
                .thenThrow(new IllegalStateException("Cannot pay for a canceled reservation."));

        mockMvc.perform(post("/api/v1/payments")
                        .with(jwt().jwt(j -> j.claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void processPayment_AmountMismatch_ReturnsBadRequest() throws Exception {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setReservationId(1L);
        request.setAmount(100000);
        request.setPaymentMethod("CREDIT_CARD");

        when(paymentService.processPayment(1L, 100000, "CREDIT_CARD", "test@example.com"))
                .thenThrow(new IllegalArgumentException("The amount must match the total reservation price: 500000"));

        mockMvc.perform(post("/api/v1/payments")
                        .with(jwt().jwt(j -> j.claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processPayment_ReservationNotFound_ReturnsNotFound() throws Exception {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setReservationId(999L);
        request.setAmount(500000);
        request.setPaymentMethod("CREDIT_CARD");

        when(paymentService.processPayment(999L, 500000, "CREDIT_CARD", "test@example.com"))
                .thenThrow(new RuntimeException("Reservation not found."));

        mockMvc.perform(post("/api/v1/payments")
                        .with(jwt().jwt(j -> j.claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}


