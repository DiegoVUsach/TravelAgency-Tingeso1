package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.service.ReservationService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;
import com.example.demo.config.SecurityConfig;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@WebMvcTest(ReservationController.class)
@Import(SecurityConfig.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== POST /api/v1/reservations/cart ====================

    @Test
    void createMultipleReservations_Authenticated_ReturnsCreated() throws Exception {
        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(2);
        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        ReservationResponseDTO response = new ReservationResponseDTO();
        response.setMessage("Reservas creadas exitosamente.");
        response.setSubtotal(200000);
        response.setFinalTotal(200000);
        response.setTotalDiscount(0);
        response.setGeneratedReservationIds(List.of(1L));
        response.setAppliedDiscounts(Collections.emptyList());

        when(reservationService.processCartReservations(any(ReservationRequestDTO.class), eq("test@example.com")))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/reservations/cart")
                        .with(jwt().jwt(j -> j.claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Reservas creadas exitosamente."))
                .andExpect(jsonPath("$.subtotal").value(200000));
    }

    @Test
    void createMultipleReservations_Unauthenticated_ReturnsUnauthorized() throws Exception {
        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(2);
        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/v1/reservations/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createMultipleReservations_EmptyCart_ReturnsBadRequest() throws Exception {
        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(Collections.emptyList());

        when(reservationService.processCartReservations(any(ReservationRequestDTO.class), eq("test@example.com")))
                .thenThrow(new IllegalArgumentException("El carrito no puede estar vacío."));

        mockMvc.perform(post("/api/v1/reservations/cart")
                        .with(jwt().jwt(j -> j.claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== POST /api/v1/reservations/quote ====================

    @Test
    void quoteReservation_Authenticated_ReturnsOk() throws Exception {
        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(2);
        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        ReservationResponseDTO response = new ReservationResponseDTO();
        response.setMessage("Cotización calculada exitosamente.");
        response.setSubtotal(200000);
        response.setFinalTotal(180000);
        response.setTotalDiscount(20000);
        response.setAppliedDiscounts(Collections.emptyList());

        when(reservationService.calculateQuote(any(ReservationRequestDTO.class), eq("test@example.com")))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/reservations/quote")
                        .with(jwt().jwt(j -> j.claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cotización calculada exitosamente."));
    }

    // ==================== GET /api/v1/reservations/my-reservations ====================

    @Test
    void getMyReservations_Authenticated_ReturnsOk() throws Exception {
        when(reservationService.getUserReservations("test@example.com"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/reservations/my-reservations")
                        .with(jwt().jwt(j -> j.claim("email", "test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getMyReservations_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/reservations/my-reservations"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== GET /api/v1/reservations/all (admin) ====================

    @Test
    void getAllReservations_AsAdmin_ReturnsOk() throws Exception {
        when(reservationService.getAllReservations()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/reservations/all")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void getAllReservations_AsClient_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/reservations/all")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }

    // ==================== PATCH /api/v1/reservations/{id}/state (admin) ====================

    @Test
    void updateReservationState_AsAdmin_ReturnsOk() throws Exception {
        ReservationEntity updated = new ReservationEntity();
        updated.setId(1L);
        updated.setState(ReservationState.CONFIRMED);

        when(reservationService.updateReservationState(1L, ReservationState.CONFIRMED))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/v1/reservations/1/state")
                        .param("newState", "CONFIRMED")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void updateReservationState_AsClient_ReturnsForbidden() throws Exception {
        mockMvc.perform(patch("/api/v1/reservations/1/state")
                        .param("newState", "CONFIRMED")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }

    // ==================== GET /api/v1/reservations/{id}/receipt ====================

    @Test
    void getReservationReceipt_Authenticated_ReturnsOk() throws Exception {
        ReservationReceiptDTO receipt = new ReservationReceiptDTO();
        receipt.setReceiptCode("REC-1-2026");
        receipt.setIssueDate(LocalDate.now());
        receipt.setClientEmail("test@example.com");
        receipt.setBundleName("Caribe");
        receipt.setDestination("Punta Cana");
        receipt.setNumberOfPassengers(2);
        receipt.setTotalPaid(200000);
        receipt.setStatus("PAGADO OFICIALMENTE");

        when(reservationService.generateReceipt(eq(1L), eq("test@example.com"), anyBoolean()))
                .thenReturn(receipt);

        mockMvc.perform(get("/api/v1/reservations/1/receipt")
                        .with(jwt().jwt(j -> j.claim("email", "test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptCode").value("REC-1-2026"))
                .andExpect(jsonPath("$.status").value("PAGADO OFICIALMENTE"));
    }

    @Test
    void getReservationReceipt_NotOwner_ReturnsConflict() throws Exception {
        when(reservationService.generateReceipt(eq(1L), eq("other@example.com"), anyBoolean()))
                .thenThrow(new IllegalStateException("Solo puedes acceder a los recibos de tus propias reservas."));

        mockMvc.perform(get("/api/v1/reservations/1/receipt")
                        .with(jwt().jwt(j -> j.claim("email", "other@example.com"))))
                .andExpect(status().isConflict());
    }

    // ==================== GET /api/v1/reservations/reports/sales (admin) ====================

    @Test
    void getSalesReport_AsAdmin_ReturnsOk() throws Exception {
        when(reservationService.getSalesByPeriod(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/reservations/reports/sales")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void getSalesReport_AsClient_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/reservations/reports/sales")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }

    // ==================== GET /api/v1/reservations/reports/ranking (admin) ====================

    @Test
    void getPackageRanking_AsAdmin_ReturnsOk() throws Exception {
        when(reservationService.getPackageRanking(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/reservations/reports/ranking")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void getPackageRanking_AsClient_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/reservations/reports/ranking")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }
}


