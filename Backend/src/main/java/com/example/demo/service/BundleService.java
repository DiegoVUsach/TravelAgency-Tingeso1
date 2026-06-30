package com.example.demo.service;


import com.example.demo.entity.BundleEntity;
import com.example.demo.entity.BundleState;
import com.example.demo.entity.ExperienceTypeState;
import com.example.demo.entity.SeasonTypeState;
import com.example.demo.entity.CategoryTypeState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.repository.BundleRepository;
import com.example.demo.repository.ReservationRepository;



import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
public class BundleService {

    private final BundleRepository bundleRepository;
    private final ReservationRepository reservationRepository;

    @Autowired
    public BundleService(BundleRepository bundleRepository, ReservationRepository reservationRepository) {
        this.bundleRepository = bundleRepository;
        this.reservationRepository = reservationRepository;
    }

    // ---------- Public reads ----------

    public List<BundleEntity> findByPriceBundleGreaterThan(int price) {
        return bundleRepository.findByPriceBundleGreaterThan(price);
    }

    public BundleEntity getBundleById(Long id) {
        return bundleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bundle not found with id: " + id));
    }

    // E3 method, for bundle search
    public List<BundleEntity> searchAvailableBundles(
            String destination,
            Integer minPrice,
            Integer maxPrice,
            Integer duration,
            LocalDate startDate,
            LocalDate endDate,
            ExperienceTypeState experience,
            SeasonTypeState season,
            CategoryTypeState category) {

        return bundleRepository.searchAvailableBundles(
                BundleState.AVAILABLE,
                destination,
                minPrice,
                maxPrice,
                duration,
                startDate,
                endDate,
                experience,
                season,
                category
        );
    }

    // ---------- Admin CRUD ----------

    public BundleEntity saveBundle(BundleEntity bundleEntity) {
        // Validate required fields
        if (bundleEntity.getNameBundle() == null || bundleEntity.getNameBundle().isBlank()) {
            throw new IllegalArgumentException("Package name is required.");
        }
        if (bundleEntity.getDestinationBundle() == null || bundleEntity.getDestinationBundle().isBlank()) {
            throw new IllegalArgumentException("Destination is required.");
        }
        if (bundleEntity.getDescriptionBundle() == null || bundleEntity.getDescriptionBundle().isBlank()) {
            throw new IllegalArgumentException("Description is required.");
        }

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
        if (bundleEntity.getExperienceTypes() == null || bundleEntity.getExperienceTypes().isEmpty()) {
            throw new IllegalArgumentException("At least one experience type must be specified.");
        }

        // Cannot publish as AVAILABLE if no slots
        if (bundleEntity.getStateBundle() == BundleState.AVAILABLE && bundleEntity.getAvailableSlotsBundle() <= 0) {
            throw new IllegalArgumentException("Cannot publish as available if there are no spots.");
        }

        // Auto-calculate duration from dates
        int calculatedDuration = (int) ChronoUnit.DAYS.between(
                bundleEntity.getStartDateBundle(),
                bundleEntity.getEndDateBundle()
        );
        bundleEntity.setDurationBundle(calculatedDuration);

        // Set default state if not provided
        if (bundleEntity.getStateBundle() == null) {
            bundleEntity.setStateBundle(BundleState.AVAILABLE);
        }

        return bundleRepository.save(bundleEntity);
    }

    public BundleEntity updateBundle(Long id, BundleEntity newDetails) {
        BundleEntity existingBundle = bundleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bundle not found"));

        long currentReservations = reservationRepository.countByBundleIdBundle(id);

        // Validate required fields
        if (newDetails.getNameBundle() == null || newDetails.getNameBundle().isBlank()) {
            throw new IllegalArgumentException("Package name is required.");
        }
        if (newDetails.getDestinationBundle() == null || newDetails.getDestinationBundle().isBlank()) {
            throw new IllegalArgumentException("Destination is required.");
        }
        if (newDetails.getDescriptionBundle() == null || newDetails.getDescriptionBundle().isBlank()) {
            throw new IllegalArgumentException("Description is required.");
        }
        if (newDetails.getPriceBundle() <= 0) {
            throw new IllegalArgumentException("Price must be above 0 CLP.");
        }

        // Cannot publish as AVAILABLE if no slots
        if (newDetails.getStateBundle() == BundleState.AVAILABLE && newDetails.getAvailableSlotsBundle() <= 0) {
            throw new IllegalArgumentException("Cannot publish as available if there are no spots.");
        }

        if (currentReservations > 0) {
            // Cannot modify critical fields if there are reservations
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
            existingBundle.setPriceBundle(newDetails.getPriceBundle());
        }

        existingBundle.setNameBundle(newDetails.getNameBundle());
        existingBundle.setDestinationBundle(newDetails.getDestinationBundle());
        existingBundle.setDescriptionBundle(newDetails.getDescriptionBundle());
        existingBundle.setExperienceTypes(newDetails.getExperienceTypes());
        existingBundle.setSeasonType(newDetails.getSeasonType());
        existingBundle.setCategoryType(newDetails.getCategoryType());
        existingBundle.setAvailableSlotsBundle(newDetails.getAvailableSlotsBundle());
        existingBundle.setStateBundle(newDetails.getStateBundle());

        // Update detail fields
        existingBundle.setIncludedServices(newDetails.getIncludedServices());
        existingBundle.setConditions(newDetails.getConditions());
        existingBundle.setRestrictions(newDetails.getRestrictions());

        // Update promo fields
        existingBundle.setPromoStartDate(newDetails.getPromoStartDate());
        existingBundle.setPromoEndDate(newDetails.getPromoEndDate());
        existingBundle.setPromoDiscountPercent(newDetails.getPromoDiscountPercent());

        // Auto-calculate duration from dates
        int recalculatedDuration = (int) ChronoUnit.DAYS.between(
                existingBundle.getStartDateBundle(),
                existingBundle.getEndDateBundle()
        );
        existingBundle.setDurationBundle(recalculatedDuration);

        // Auto-set state to SOLD_OUT if slots reach 0
        if (existingBundle.getAvailableSlotsBundle() <= 0 && existingBundle.getStateBundle() == BundleState.AVAILABLE) {
            existingBundle.setStateBundle(BundleState.SOLD_OUT);
        }

        return bundleRepository.save(existingBundle);
    }

    public void deleteBundle(Long id) {
        BundleEntity existingBundle = bundleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bundle not found"));

        long reservationCount = reservationRepository.countByBundleIdBundle(id);

        if (reservationCount > 0) {
            // Logical deletion — never physically delete bundles with reservations
            existingBundle.setStateBundle(BundleState.CANCELED);
            bundleRepository.save(existingBundle);
        } else {
            bundleRepository.delete(existingBundle);
        }
    }


}
