package com.example.demo.controller;

import com.example.demo.dto.ReservationRequestDTO;
import com.example.demo.dto.ReservationResponseDTO;
import com.example.demo.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/cart")
    public ResponseEntity<ReservationResponseDTO> createMultipleReservations(@RequestBody ReservationRequestDTO request) {

        ReservationResponseDTO response = reservationService.processCartReservations(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}