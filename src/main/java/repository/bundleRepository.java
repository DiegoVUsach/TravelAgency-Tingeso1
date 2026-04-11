package repository;

import entity.bundleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // add gets, set, etc


public interface bundleRepository extends JpaRepository<bundleEntity,Long> {
    //add necesary declaration of method here

    List<bundleEntity> findByPriceBundleGreaterThan(int price);
}

