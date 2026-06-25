package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Set;


@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(name = "Bundles")

// attributes:  name, destination, description, available dates, duration, price, included services,
//              conditions, restrictions, available slots, experience types, state.
public class BundleEntity {


    // primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bundle")
    private Long idBundle;

    // ========== Basic Fields ==========

    @Column(name = "name_bundle", length = 80, nullable = false)
    private String nameBundle;

    @Column(name = "destination_bundle", length = 80, nullable = false)
    private String destinationBundle;

    @Column(name = "description_bundle", length = 1000, nullable = false)
    private String descriptionBundle;

    @Column(name = "start_date_bundle", nullable = false)
    private LocalDate startDateBundle;

    @Column(name = "end_date_bundle", nullable = false)
    private LocalDate endDateBundle;

    @Column(name = "duration_bundle", nullable = false)
    private int durationBundle; // auto-calculated from startDate and endDate

    @Column(name = "price_bundle", nullable = false)
    private int priceBundle;

    @Column(name = "available_slots_bundle", nullable = false)
    private int availableSlotsBundle;

    // ========== Detail & Classification Attributes ==========

    @Column(name = "included_services", length = 1000)
    private String includedServices; // e.g. "5-star hotel, airport transfers, bilingual guide"

    @Column(name = "conditions", length = 1000)
    private String conditions; // e.g. "Subject to availability, minimum 2 persons"

    @Column(name = "restrictions", length = 1000)
    private String restrictions; // e.g. "Non-refundable, minimum age 18"

    @ElementCollection(targetClass = ExperienceTypeState.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "bundle_experience_types", joinColumns = @JoinColumn(name = "bundle_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "experience_type")
    private Set<ExperienceTypeState> experienceTypes; // allows multiple: e.g. ROMANTIC + FAMILY

    @Enumerated(EnumType.STRING)
    @Column(name = "state_bundle", nullable = false)
    private BundleState stateBundle;

    // ========== Promotions ==========

    @Column(nullable = true)
    private LocalDate promoStartDate;

    @Column(nullable = true)
    private LocalDate promoEndDate;

    @Column(nullable = true)
    private Double promoDiscountPercent; // e.g. 0.10 for 10% discount
}
