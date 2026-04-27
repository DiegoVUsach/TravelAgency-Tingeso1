package com.example.demo.repository;

import com.example.demo.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    long countByBundleIdBundle(Long idBundle); // count how many reservations currently are for a bundle
}