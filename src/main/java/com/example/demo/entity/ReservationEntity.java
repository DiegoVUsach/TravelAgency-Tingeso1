package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(name = "Reservations")

public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TBI keycloak stuff here
    @Column(nullable = false)
    private String userEmail;

    // check later 02 05 2026 from hero dowm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_id", nullable = false)
    private BundleEntity bundle;

    @Column(nullable = false)
    private Integer numberOfPassengers;

    @Column(nullable = false)
    private LocalDate reservationDate;

    @Column(nullable = false)
    private Integer totalAmount; // Monto final tras aplicar los descuentos

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationState state;
}