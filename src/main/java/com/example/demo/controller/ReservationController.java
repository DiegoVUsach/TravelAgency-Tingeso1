package com.example.demo.controller;

import com.example.demo.entity.ReservationEntity;
import com.example.demo.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationEntity> createReservation(@RequestBody ReservationRequest request) {
        ReservationEntity newReservation = reservationService.createReservation(
                request.userEmail(),
                request.bundleId(),
                request.passengers()
        );


        return new ResponseEntity<>(newReservation, HttpStatus.CREATED);
    }

    // ask martin about this, this is a dto workalike that should help with json req
    public record ReservationRequest(String userEmail, Long bundleId, Integer passengers) {}
}