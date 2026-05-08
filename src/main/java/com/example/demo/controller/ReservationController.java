package com.example.demo.controller;

import com.example.demo.dto.ReservationReceiptDTO;
import com.example.demo.dto.ReservationRequestDTO;
import com.example.demo.dto.ReservationResponseDTO;
import com.example.demo.entity.ReservationEntity;
import com.example.demo.entity.ReservationState;
import com.example.demo.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    // E6
    // MUST IMPLEMENT KEYCLOAK HERE INSTEAD OF STR MAIL
    @GetMapping("/my-reservations")
    public ResponseEntity<List<ReservationEntity>> getMyReservations(@RequestParam String email) {
        List<ReservationEntity> reservations = reservationService.getUserReservations(email);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReservationEntity>> getAllReservations() {
        List<ReservationEntity> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }

    // change the state of a reservation
    @PatchMapping("/{id}/state")
    public ResponseEntity<ReservationEntity> updateReservationState(
            @PathVariable Long id,
            @RequestParam ReservationState newState) {

        ReservationEntity updatedReservation = reservationService.updateReservationState(id, newState);
        return ResponseEntity.ok(updatedReservation);
    }

    // E6
    @GetMapping("/{id}/receipt")
    public ResponseEntity<ReservationReceiptDTO> getReservationReceipt(@PathVariable Long id) {
        ReservationReceiptDTO receipt = reservationService.generateReceipt(id);
        return ResponseEntity.ok(receipt);
    }
}