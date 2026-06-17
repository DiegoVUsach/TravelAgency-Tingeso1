package com.example.demo.controller;

import com.example.demo.dto.PaymentRequestDTO;
import com.example.demo.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> processPayment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody PaymentRequestDTO request) {

        String callerEmail = jwt.getClaimAsString("email");
        String result = paymentService.processPayment(
                request.getReservationId(),
                request.getAmount(),
                request.getPaymentMethod(),
                callerEmail);

        return ResponseEntity.ok(result);
    }
}