package com.slayer.backend.service;

import com.slayer.backend.payload.dto.ReservationDTO;
import com.slayer.backend.payload.reponse.PageResponse;
import com.slayer.backend.payload.request.ReservationRequest;
import com.slayer.backend.payload.request.ReservationSearchRequest;
import org.springframework.data.domain.PageRequest;

public interface ReservationServiceInterface {

    ReservationDTO createReservation(ReservationRequest request);

    ReservationDTO createReservationForUser(ReservationRequest request, Long userId);

    ReservationDTO cancelReservation(Long reservationId);

    ReservationDTO readyReservation(Long reservationId);

    ReservationDTO rejectReservation(Long reservationId);

    ReservationDTO userConfirmReservation(Long reservationId);

    ReservationDTO confirmBorrowed(Long reservationId);

    PageResponse<ReservationDTO> getMyReservations(ReservationSearchRequest request);

    PageResponse<ReservationDTO> searchReservations(ReservationSearchRequest request);
}
