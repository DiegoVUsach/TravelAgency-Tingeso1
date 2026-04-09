package entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(name = "Bundle")

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
    private String startDateBundle; // to do: change to date type

    @Column(name = "end_date_bundle", nullable = false)
    private String endDateBundle; // to do: change to date type and must end after start date

    @Column(name = "price_bundle", nullable = false)
    private int priceBundle; // to do: add restrictions for price (must be positive and not 0)

    @Column(name = "available_slots_bundle", nullable = false)
    private int availableSlotsBundle; // to do: add restrictions for available slots (must be positive and not 0)

    //to do: add the rest.
}
