package com.example.demo.service;


import com.example.demo.entity.BundleEntity;
import com.example.demo.entity.BundleState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.repository.BundleRepository;
import com.example.demo.repository.ReservationRepository;



import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BundleService {

    private final BundleRepository bundleRepository;
    private final ReservationRepository reservationRepository;

    @Autowired
    public BundleService(BundleRepository bundleRepository, ReservationRepository reservationRepository) {
        this.bundleRepository = bundleRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<BundleEntity> findByPriceBundleGreaterThan(int price) {
        return bundleRepository.findByPriceBundleGreaterThan(price);
    }

    // E3 method, for bundle search
    public List<BundleEntity> searchAvailableBundles(
            String destiny,
            Integer minPrice,
            Integer maxPrice,
            Integer duration,
            LocalDate startDate,
            LocalDate endDate) {

        return bundleRepository.searchAvailableBundles(
                BundleState.AVAILABLE,
                destiny,
                minPrice,
                maxPrice,
                duration,
                startDate,
                endDate
        );
    }

    public BundleEntity saveBundle(BundleEntity bundleEntity) {
        // H2 validations
        if (bundleEntity.getPriceBundle() <= 0) {
            throw new IllegalArgumentException("Price must be above 0 CLP.");
        }
        if (bundleEntity.getAvailableSlotsBundle() <= 0) {
            throw new IllegalArgumentException("Available slots must be above 0.");
        }
        if (bundleEntity.getStartDateBundle() == null || bundleEntity.getEndDateBundle() == null ||
                !bundleEntity.getStartDateBundle().isBefore(bundleEntity.getEndDateBundle())) {
            throw new IllegalArgumentException("The start date must be before the end date and not null.");
        }
        if (bundleEntity.getTipoExperienciaBundle() == null) {
            throw new IllegalArgumentException("The experience type must be specified.");
        }

        int calculatedDuration = (int) ChronoUnit.DAYS.between(
                bundleEntity.getStartDateBundle(),
                bundleEntity.getEndDateBundle()
        );
        bundleEntity.setDurationBundle(calculatedDuration); // automatic duration calc

        return bundleRepository.save(bundleEntity);
    }

    public BundleEntity updateBundle(Long id, BundleEntity newDetails) {
        BundleEntity existingBundle = bundleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bundle not found"));

        long currentReservations = reservationRepository.countByBundleIdBundle(id);

        if (currentReservations > 0) {
            if (!existingBundle.getStartDateBundle().equals(newDetails.getStartDateBundle()) ||
                    !existingBundle.getEndDateBundle().equals(newDetails.getEndDateBundle()) ||
                    existingBundle.getPriceBundle() != newDetails.getPriceBundle()) {
                throw new IllegalStateException("You cannot modify the price/date: there are already reservations in place.");
            }

            if (newDetails.getAvailableSlotsBundle() < currentReservations) {
                throw new IllegalStateException("You cannot reduce the total slots to " + newDetails.getAvailableSlotsBundle() +
                        " because there are already " + currentReservations + " reservations registered.");
            }
        } else {
            if (newDetails.getAvailableSlotsBundle() <= 0) {
                throw new IllegalArgumentException("Available slots must be above 0.");
            }
            existingBundle.setStartDateBundle(newDetails.getStartDateBundle());
            existingBundle.setEndDateBundle(newDetails.getEndDateBundle());
        }

        existingBundle.setNameBundle(newDetails.getNameBundle());
        existingBundle.setDestinyBundle(newDetails.getDestinyBundle());
        existingBundle.setDescBundle(newDetails.getDescBundle());
        existingBundle.setTipoExperienciaBundle(newDetails.getTipoExperienciaBundle());
        existingBundle.setAvailableSlotsBundle(newDetails.getAvailableSlotsBundle());
        existingBundle.setStateBundle(newDetails.getStateBundle());

        int recalculatedDuration = (int) ChronoUnit.DAYS.between(
                existingBundle.getStartDateBundle(),
                existingBundle.getEndDateBundle()
        );
        existingBundle.setDurationBundle(recalculatedDuration);

        return bundleRepository.save(existingBundle);
    }

    public void deleteBundle(Long id) {
        BundleEntity existingBundle = bundleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bundle not found"));

        long reservationCount = reservationRepository.countByBundleIdBundle(id);

        if (reservationCount > 0) {
            existingBundle.setStateBundle(BundleState.CANCELED); // ask teacher about this later
            bundleRepository.save(existingBundle);
        } else {
            bundleRepository.delete(existingBundle);
        }
    }


}
