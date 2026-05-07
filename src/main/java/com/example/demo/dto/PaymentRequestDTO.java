package com.example.demo.dto;

import lombok.Data;

@Data
public class PaymentRequestDTO {

    private Long reservationId;
    private Integer amount;
    private String paymentMethod; // ex: "CREDIT_CARD"

    // simulated card data
    private String cardNumber;
    private String expirationDate;
    private String cvv;
}