package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReservationReceiptDTO {
    private String receiptCode;
    private LocalDate issueDate;
    private String clientEmail;
    private String bundleName;
    private String destination;
    private int numberOfPassengers;
    private int totalPaid;
    private String status;
}