package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountDetailDTO {

    private String type;        // e.g. "VOLUME_DISCOUNT", "FREQUENT_CLIENT", "MULTIPLE_PACKAGES", "PROMOTION"
    private String description;  // e.g. "Discount for 4+ passengers", "Frequent client (3+ paid reservations)"
    private Double percentage;   // e.g. 0.10 for 10%
    private Integer amount;      // actual CLP amount saved by this discount
}
