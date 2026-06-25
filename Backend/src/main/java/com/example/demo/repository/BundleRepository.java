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

    List<BundleEntity> findByDestinationBundleContainingIgnoreCase(String destination);


    // Query for E3 method, for bundle search with multiple optional parameters
    // experienceTypes is a @ElementCollection so we use MEMBER OF to check membership
    @Query("SELECT DISTINCT b FROM BundleEntity b LEFT JOIN b.experienceTypes et WHERE " +
            "b.stateBundle = :state AND " +
            "b.availableSlotsBundle > 0 AND " +
            "b.startDateBundle >= CURRENT_DATE AND " +
            "(:destination IS NULL OR LOWER(b.destinationBundle) LIKE LOWER(CONCAT('%', :destination, '%'))) AND " +
            "(:minPrice IS NULL OR b.priceBundle >= :minPrice) AND " +
            "(:maxPrice IS NULL OR b.priceBundle <= :maxPrice) AND " +
            "(:duration IS NULL OR b.durationBundle = :duration) AND " +
            "(:startDate IS NULL OR b.startDateBundle >= :startDate) AND " +
            "(:endDate IS NULL OR b.endDateBundle <= :endDate) AND " +
            "(:experience IS NULL OR :experience MEMBER OF b.experienceTypes)")
    List<BundleEntity> searchAvailableBundles(
            @Param("state") BundleState state,
            @Param("destination") String destination,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("duration") Integer duration,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("experience") ExperienceTypeState experience
    );


}

