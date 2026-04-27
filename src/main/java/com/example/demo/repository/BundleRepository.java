package com.example.demo.repository;

import com.example.demo.entity.BundleEntity;
import com.example.demo.entity.BundleState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BundleRepository extends JpaRepository<BundleEntity,Long> {

    List<BundleEntity> findByPriceBundleGreaterThan(int price);

    List<BundleEntity> findByStateBundle(BundleState state);

    List<BundleEntity> findByDestinyBundleContainingIgnoreCase(String destiny);

}

