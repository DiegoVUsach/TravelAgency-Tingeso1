package com.example.demo.repository;

import com.example.demo.entity.BundleEntity;
import com.example.demo.entity.BundleState;
import com.example.demo.entity.ExperienceTypeState;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BundleRepository extends JpaRepository<BundleEntity,Long> {

    List<BundleEntity> findByPriceBundleGreaterThan(int price);

    List<BundleEntity> findByStateBundle(BundleState state);

    List<BundleEntity> findByDestinyBundleContainingIgnoreCase(String destiny);

    // tbd if the query is useful when front is implemented
    @Query("SELECT b FROM BundleEntity b WHERE " +
            "b.stateBundle = :state AND " +
            "b.availableSlotsBundle > 0 AND " +
            "b.startDateBundle >= CURRENT_DATE AND " +
            "(:destiny IS NULL OR LOWER(b.destinyBundle) LIKE LOWER(CONCAT('%', :destiny, '%'))) AND " +
            "(:minPrice IS NULL OR b.priceBundle >= :minPrice) AND " +
            "(:maxPrice IS NULL OR b.priceBundle <= :maxPrice) AND " +
            "(:duration IS NULL OR b.durationBundle = :duration) AND " +
            "(:startDate IS NULL OR b.startDateBundle >= :startDate) AND " +
            "(:endDate IS NULL OR b.endDateBundle <= :endDate) AND " +
            "(:experience IS NULL OR b.experienceType = :experience)")
    List<BundleEntity> searchAvailableBundles(
            @Param("state") BundleState state,
            @Param("destiny") String destiny,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("duration") Integer duration,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("experience") ExperienceTypeState experience
    );

}

