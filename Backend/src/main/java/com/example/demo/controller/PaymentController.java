package com.example.demo.controller;

import com.example.demo.dto.PaymentRequestDTO;
import com.example.demo.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin("*")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<String> processPayment(@RequestBody PaymentRequestDTO request) {

        String result = paymentService.processPayment(
                request.getReservationId(),
                request.getAmount(),
                request.getPaymentMethod()
        );

        return ResponseEntity.ok(result);
    }
}