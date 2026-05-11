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
@Table(name = "Bundles")

// attributes:  name, destiny, desc, available dates, duration, price, included services,
//              conditions, restrictions, available slots.
public class BundleEntity {


    // primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bundle")
    private Long idBundle;

    // basic fields

    @Column(name = "name_bundle", length = 80, nullable = false)
    private String nameBundle;

    @Column(name = "destiny_bundle", length = 80, nullable = false)
    private String destinyBundle;

    @Column(name = "desc_bundle", length = 255, nullable = false)
    private String descBundle;

    @Column(name = "start_date_bundle", nullable = false)
    private LocalDate startDateBundle;

    @Column(name = "end_date_bundle", nullable = false)
    private LocalDate endDateBundle;

    @Column(name = "duration_bundle", nullable = false)
    private int durationBundle;

    @Column(name = "price_bundle", nullable = false)
    private int priceBundle;

    @Column(name = "available_slots_bundle", nullable = false)
    private int availableSlotsBundle; // to do: add restrictions for available slots (must be positive and not 0)

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_experiencia_bundle", nullable = false)
    private ExperienceTypeState tipoExperienciaBundle;

    @Enumerated(EnumType.STRING)
    @Column(name = "state_bundle", nullable = false)
    private BundleState stateBundle;

    @Column(name = "can_be_modified", nullable = false) //modify this, it has 2 tiers of modification, for it to be deleted at all and the other for modifying the bundle
    private boolean canBeModified; // to do: add restrictions for modification (it cannot be modified if it already has a reservation, according to the business rules)

    //category, to add later for ease of filtering bundles
}
