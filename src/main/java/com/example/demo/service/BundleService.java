package com.example.demo.service;


import com.example.demo.entity.BundleEntity;
import com.example.demo.entity.BundleState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.repository.BundleRepository;
import com.example.demo.repository.ReservationRepository;

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

        return bundleRepository.save(bundleEntity);
    }

    public BundleEntity updateBundle(Long id, BundleEntity newDetails) {
        BundleEntity existingBundle = bundleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bundle not found"));

        long currentReservations = reservationRepository.countByBundleIdBundle(id);

        if (currentReservations > 0) {
            // No se pueden modificar fechas ni precio si ya hay reservas
            if (!existingBundle.getStartDateBundle().equals(newDetails.getStartDateBundle()) ||
                    !existingBundle.getEndDateBundle().equals(newDetails.getEndDateBundle()) ||
                    existingBundle.getPriceBundle() != newDetails.getPriceBundle()) {
                throw new IllegalStateException("No se pueden modificar fechas o precio: el paquete ya tiene reservas registradas.");
            }

            // No se puede bajar el cupo total a un número menor de los que ya están reservados
            if (newDetails.getAvailableSlotsBundle() < currentReservations) {
                throw new IllegalStateException("No se puede reducir el cupo total a " + newDetails.getAvailableSlotsBundle() +
                        " porque ya existen " + currentReservations + " reservas registradas.");
            }
        } else {
            // Si no hay reservas, aún debemos validar que el nuevo cupo no sea <= 0
            if (newDetails.getAvailableSlotsBundle() <= 0) {
                throw new IllegalArgumentException("Los cupos deben ser mayores a 0.");
            }
        }

        // 3. Si pasa todas las validaciones, actualizamos los campos
        existingBundle.setNameBundle(newDetails.getNameBundle());
        existingBundle.setDestinyBundle(newDetails.getDestinyBundle());
        existingBundle.setDescBundle(newDetails.getDescBundle());
        existingBundle.setAvailableSlotsBundle(newDetails.getAvailableSlotsBundle());
        existingBundle.setStateBundle(newDetails.getStateBundle());

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
