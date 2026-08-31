package com.slayer.backend.controller;

import com.slayer.backend.Enums.ReservationStatus;
import com.slayer.backend.payload.dto.ReservationDTO;
import com.slayer.backend.payload.reponse.ApiResponse;
import com.slayer.backend.payload.reponse.PageResponse;
import com.slayer.backend.payload.request.ReservationRequest;
import com.slayer.backend.payload.request.ReservationSearchRequest;
import com.slayer.backend.service.impl.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<?> createReservation(@Valid @RequestBody ReservationRequest request) {
        try {
            ReservationDTO reservation = reservationService.createReservation(request);
            return new ResponseEntity<>(reservation, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), false));
        }
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<?> createReservationForUser(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody ReservationRequest request) {
        try {
            ReservationDTO reservation = reservationService.createReservationForUser(request, userId);
            return new ResponseEntity<>(reservation, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), false));
        }
    }

    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable("reservationId") Long reservationId) {
        try {
            ReservationDTO reservation = reservationService.cancelReservation(reservationId);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), false));
        }
    }

    @PutMapping("/{reservationId}/ready")
    public ResponseEntity<?> readyReservation(@PathVariable("reservationId") Long reservationId) {
        try {
            ReservationDTO reservation = reservationService.readyReservation(reservationId);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), false));
        }
    }

    @PutMapping("/{reservationId}/reject")
    public ResponseEntity<?> rejectReservation(@PathVariable("reservationId") Long reservationId) {
        try {
            ReservationDTO reservation = reservationService.rejectReservation(reservationId);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), false));
        }
    }

    @PutMapping("/{reservationId}/user-confirm")
    public ResponseEntity<?> userConfirmReservation(@PathVariable("reservationId") Long reservationId) {
        try {
            ReservationDTO reservation = reservationService.userConfirmReservation(reservationId);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), false));
        }
    }

    @PutMapping("/{reservationId}/confirm-borrowed")
    public ResponseEntity<?> confirmBorrowed(@PathVariable("reservationId") Long reservationId) {
        try {
            ReservationDTO reservation = reservationService.confirmBorrowed(reservationId);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), false));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyReservations(
            @ModelAttribute ReservationSearchRequest request) {
        try {
            PageResponse<ReservationDTO> reservations = reservationService.getMyReservations(request);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), false));
        }
    }

    @GetMapping
    public ResponseEntity<PageResponse<ReservationDTO>> searchReservations(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "reservedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        ReservationSearchRequest request = ReservationSearchRequest.builder()
                .userId(userId)
                .bookId(bookId)
                .status(status)
                .activeOnly(activeOnly)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        PageResponse<ReservationDTO> reservations = reservationService.searchReservations(request);
        return ResponseEntity.ok(reservations);
    }
}
