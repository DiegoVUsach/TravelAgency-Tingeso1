package com.example.demo.repository;

import com.example.demo.dto.PackageRankingDTO;
import com.example.demo.entity.ReservationEntity;
import com.example.demo.entity.ReservationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;


@Repository

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    // count how many reservations currently are for a bundle
    long countByBundleIdBundle(Long idBundle);

    // counts how  many reservations a user has in a certain state, for example, how many pending payments a user has
    long countByUserEmailAndState(String userEmail, ReservationState state);


    List<ReservationEntity> findByUserEmailOrderByReservationDateDesc(String userEmail);


    // for timeout of bundles
    List<ReservationEntity> findByStateAndReservationDateBefore(ReservationState state, LocalDate date);

    // e7, bundle ranking
    @Query("SELECT r.bundle.idBundle AS bundleId, r.bundle.nameBundle AS bundleName, " +
            "COUNT(r) AS totalReservations, " +
            "SUM(r.numberOfPassengers) AS totalPassengers, " +
            "SUM(r.totalAmount) AS totalRevenue " +
            "FROM ReservationEntity r " +
            "WHERE r.state <> :canceledState " +
            "GROUP BY r.bundle.idBundle, r.bundle.nameBundle " +
            "ORDER BY totalReservations DESC")
    List<PackageRankingDTO> findPackageRanking(@Param("canceledState") ReservationState canceledState);

    // e7
    @Query("SELECT r FROM ReservationEntity r " +
            "WHERE r.reservationDate >= :startDate " +
            "AND r.reservationDate <= :endDate " +
            "AND r.state <> :canceledState " +
            "ORDER BY r.reservationDate ASC")
    // e7
    List<ReservationEntity> findSalesByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("canceledState") ReservationState canceledState);
}