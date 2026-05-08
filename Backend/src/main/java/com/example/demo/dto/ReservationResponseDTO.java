package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class ReservationResponseDTO {
    private String message;
    private Integer subtotal;
    private Integer totalDiscount;
    private Integer finalTotal;
    private List<Long> generatedReservationIds;
}