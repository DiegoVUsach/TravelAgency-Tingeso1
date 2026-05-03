package com.example.demo.controller;

import com.example.demo.entity.ReservationEntity;
import com.example.demo.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// check this later 03 05 2026 4am
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Endpoint para crear una nueva reserva.
     * Recibe un JSON, lo mapea al record ReservationRequest y llama al servicio.
     */
    @PostMapping
    public ResponseEntity<ReservationEntity> createReservation(@RequestBody ReservationRequest request) {
        // Llamamos a la lógica pesada que hicimos en el Service
        ReservationEntity newReservation = reservationService.createReservation(
                request.userEmail(),
                request.bundleId(),
                request.passengers()
        );

        // Devolvemos la reserva creada y un código HTTP 201 (Created)
        return new ResponseEntity<>(newReservation, HttpStatus.CREATED);
    }

    /**
     * DTO simple y eficiente para recibir los datos del frontend.
     * Los 'record' en Java son inmutables y generan automáticamente
     * lo necesario para mapear el JSON.
     */
    public record ReservationRequest(String userEmail, Long bundleId, Integer passengers) {}
}