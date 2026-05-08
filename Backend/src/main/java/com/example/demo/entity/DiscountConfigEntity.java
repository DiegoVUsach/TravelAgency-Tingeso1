package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "discount_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor

//revisar desp
public class DiscountConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre descriptivo (ej: "VOLUME_DISCOUNT", "FREQUENT_CLIENT")
    @Column(unique = true, nullable = false)
    private String configKey;

    // Valor numérico (ej: 0.10 para 10%)
    @Column(nullable = false)
    private Double configValue;

    // Umbral de activación (ej: 4 pasajeros o 3 reservas previas)
    private Integer threshold;

    private String description;
}