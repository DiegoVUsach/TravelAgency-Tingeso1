package com.example.demo.controller;

import com.example.demo.dto.PackageRankingDTO;
import com.example.demo.dto.ReservationReceiptDTO;
import com.example.demo.dto.ReservationRequestDTO;
import com.example.demo.dto.ReservationResponseDTO;
import com.example.demo.entity.ReservationEntity;
import com.example.demo.entity.ReservationState;
import com.example.demo.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
public class ReservationController {

    @Autowired
    private final ReservationService reservationService;

    @PostMapping("/cart")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationResponseDTO> createMultipleReservations(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ReservationRequestDTO request) {

        String email = jwt.getClaimAsString("email");
        ReservationResponseDTO response = reservationService.processCartReservations(request, email);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/quote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationResponseDTO> quoteReservation(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ReservationRequestDTO request) {

        String email = jwt.getClaimAsString("email");
        ReservationResponseDTO quote = reservationService.calculateQuote(request, email);
        return ResponseEntity.ok(quote);
    } // E6

    @GetMapping("/my-reservations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReservationEntity>> getMyReservations(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        List<ReservationEntity> reservations = reservationService.getUserReservations(email);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<ReservationEntity>> getAllReservations() {
        List<ReservationEntity> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }

    // change the state of a reservation
    @PatchMapping("/{id}/state")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ReservationEntity> updateReservationState(
            @PathVariable Long id,
            @RequestParam ReservationState newState) {

        ReservationEntity updatedReservation = reservationService.updateReservationState(id, newState);
        return ResponseEntity.ok(updatedReservation);
    }

    // E6
    @GetMapping("/{id}/receipt")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationReceiptDTO> getReservationReceipt(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String callerEmail = jwt.getClaimAsString("email");
        boolean isAdmin = jwt.getClaimAsStringList("realm_access") != null &&
                jwt.getClaim("realm_access") instanceof java.util.Map<?, ?> realmAccess &&
                realmAccess.get("roles") instanceof java.util.List<?> roles &&
                roles.contains("ADMIN");
        ReservationReceiptDTO receipt = reservationService.generateReceipt(id, callerEmail, isAdmin);
        return ResponseEntity.ok(receipt);
    }

    // e7
    /**
     * Report 1: Sales List by Period
     * GET
     * /api/v1/reservations/reports/sales?startDate=2026-01-01&endDate=2026-12-31
     */
    @GetMapping("/reports/sales")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<ReservationEntity>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<ReservationEntity> sales = reservationService.getSalesByPeriod(startDate, endDate);
        return ResponseEntity.ok(sales);
    }

    /**
     * Report 2: Package Sales Ranking by Period
     * GET
     * /api/v1/reservations/reports/ranking?startDate=2026-01-01&endDate=2026-12-31
     */
    @GetMapping("/reports/ranking")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<PackageRankingDTO>> getPackageRanking(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PackageRankingDTO> ranking = reservationService.getPackageRanking(startDate, endDate);
        return ResponseEntity.ok(ranking);
    }
}