package entity;

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
public class bundleEntity {

    // primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bundle")
    private Long idBundle;

    // basic fields
    // must have fields (name, destiny, desc., start date, end date, price and available slots)

    @Column(name = "name_bundle", length = 80, nullable = false)
    private String nameBundle;

    @Column(name = "destiny_bundle", length = 80, nullable = false)
    private String destinyBundle;

    @Column(name = "desc_bundle", length = 255, nullable = false)
    private String descBundle;

    @Column(name = "start_date_bundle", nullable = false)
    private LocalDate startDateBundle;

    @Column(name = "end_date_bundle", nullable = false)
    private LocalDate endDateBundle; // add proper restrictions for dates (start date must be before end date, and both must be in the future)

    @Column(name = "price_bundle", nullable = false)
    private int priceBundle; // to do: add restrictions for price (must be positive and not 0)

    @Column(name = "available_slots_bundle", nullable = false)
    private int availableSlotsBundle; // to do: add restrictions for available slots (must be positive and not 0)

    //verify this values
}
