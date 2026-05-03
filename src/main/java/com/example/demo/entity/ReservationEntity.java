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
    // TBI keycloak stuff up

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_id", nullable = false)
    private BundleEntity bundle;

    @Column(nullable = false)
    private Integer numberOfPassengers; //maybe ask teacher about this

    @Column(nullable = false)
    private LocalDate reservationDate;

    @Column(nullable = false)
    private Integer totalAmount; // fianl amount after discounts

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationState state;
}