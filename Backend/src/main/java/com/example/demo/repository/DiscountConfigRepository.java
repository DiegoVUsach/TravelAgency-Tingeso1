package com.example.demo.repository;

import com.example.demo.entity.DiscountConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DiscountConfigRepository extends JpaRepository<DiscountConfigEntity, Long> {
    Optional<DiscountConfigEntity> findByConfigKey(String configKey);
}