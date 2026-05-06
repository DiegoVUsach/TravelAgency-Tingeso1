package com.example.demo.repository;

import com.example.demo.entity.ReservationEntity;
import com.example.demo.entity.ReservationState;
import org.springframework.data.jpa.repository.JpaRepository;
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
}