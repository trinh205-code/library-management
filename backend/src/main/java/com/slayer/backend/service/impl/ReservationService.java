package com.slayer.backend.service.impl;

import com.slayer.backend.Enums.ReservationStatus;
import com.slayer.backend.entity.Book;
import com.slayer.backend.entity.Reservation;
import com.slayer.backend.entity.User;
import com.slayer.backend.mapper.ReservationMapper;
import com.slayer.backend.payload.dto.ReservationDTO;
import com.slayer.backend.payload.reponse.PageResponse;
import com.slayer.backend.payload.request.ReservationRequest;
import com.slayer.backend.payload.request.ReservationSearchRequest;
import com.slayer.backend.payload.request.CheckoutRequest;
import com.slayer.backend.repository.BookRepository;
import com.slayer.backend.repository.ReservationRepository;
import com.slayer.backend.service.ReservationServiceInterface;
import com.slayer.backend.service.BookLoanServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService implements ReservationServiceInterface {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserService userService;
    private final ReservationMapper reservationMapper;
    private final BookLoanServiceInterface bookLoanService;

    @Override
    public ReservationDTO createReservation(ReservationRequest request) {
        try {
            User user = userService.getUser();
            return createReservationForUser(request, user.getId());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ReservationDTO createReservationForUser(ReservationRequest request, Long userId) {
        try {
            User user = userService.findById(userId);

            Book book = bookRepository.findById(request.getBookId()).orElseThrow(
                    () -> new Exception("Book not found with id " + request.getBookId())
            );

            // Check if user already has an active reservation for this book
            List<ReservationStatus> activeStatuses = Arrays.asList(
                    ReservationStatus.PENDING, ReservationStatus.READY, ReservationStatus.USER_CONFIRMED
            );

            long existingCount = reservationRepository.countByUserIdAndBookIdAndStatusIn(
                    userId, book.getId(), activeStatuses
            );

            if (existingCount > 0) {
                throw new Exception("You already have an active reservation for this book");
            }

            // Calculate queue position
            int maxPosition = reservationRepository.findMaxQueuePositionByBookIdAndStatus(
                    book.getId(), ReservationStatus.PENDING
            );

            Reservation reservation = Reservation.builder()
                    .user(user)
                    .book(book)
                    .status(ReservationStatus.PENDING)
                    .reservedAt(LocalDateTime.now())
                    .queuePosition(maxPosition + 1)
                    .notificationSent(false)
                    .notes(request.getNotes())
                    .build();

            // If book is available, mark reservation as available immediately
            if (book.getAvailableCopies() != null && book.getAvailableCopies() > 0) {
                reservation.setStatus(ReservationStatus.READY);
                reservation.setAvailableAt(LocalDateTime.now());
                reservation.setAvailableUntil(LocalDateTime.now().plusHours(48));
                reservation.setQueuePosition(0);
            }

            Reservation savedReservation = reservationRepository.save(reservation);
            return reservationMapper.toDTO(savedReservation);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ReservationDTO cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                () -> new RuntimeException("Reservation not found with id " + reservationId)
        );

        if (reservation.getStatus() != ReservationStatus.PENDING
                && reservation.getStatus() != ReservationStatus.READY
                && reservation.getStatus() != ReservationStatus.USER_CONFIRMED) {
            throw new RuntimeException("Only PENDING, READY or USER_CONFIRMED reservations can be cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());

        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationMapper.toDTO(savedReservation);
    }

    @Override
    public ReservationDTO readyReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                () -> new RuntimeException("Reservation not found with id " + reservationId)
        );

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("Only PENDING reservations can be marked as READY");
        }

        reservation.setStatus(ReservationStatus.READY);
        reservation.setAvailableAt(LocalDateTime.now());
        reservation.setAvailableUntil(LocalDateTime.now().plusHours(48)); // Optional: keeping logic if needed
        
        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationMapper.toDTO(savedReservation);
    }

    @Override
    public ReservationDTO rejectReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                () -> new RuntimeException("Reservation not found with id " + reservationId)
        );

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("Only PENDING reservations can be REJECTED");
        }

        reservation.setStatus(ReservationStatus.REJECTED);
        reservation.setCancelledAt(LocalDateTime.now());
        
        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationMapper.toDTO(savedReservation);
    }

    @Override
    public ReservationDTO userConfirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                () -> new RuntimeException("Reservation not found with id " + reservationId)
        );

        if (reservation.getStatus() != ReservationStatus.READY) {
            throw new RuntimeException("Only READY reservations can be confirmed by user");
        }

        reservation.setStatus(ReservationStatus.USER_CONFIRMED);
        
        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationMapper.toDTO(savedReservation);
    }

    @Override
    public ReservationDTO confirmBorrowed(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                () -> new RuntimeException("Reservation not found with id " + reservationId)
        );

        if (reservation.getStatus() != ReservationStatus.USER_CONFIRMED && reservation.getStatus() != ReservationStatus.READY) {
            throw new RuntimeException("Only USER_CONFIRMED or READY reservations can be marked as BORROWED");
        }

        try {
            CheckoutRequest checkoutRequest = new CheckoutRequest();
            checkoutRequest.setBookId(reservation.getBook().getId());
            checkoutRequest.setCheckoutDays(14);
            checkoutRequest.setNotes("Auto checkout from reservation #" + reservationId);
            bookLoanService.checkoutBookForUser(reservation.getUser().getId(), checkoutRequest);
        } catch (Exception e) {
            throw new RuntimeException("Cannot mark as borrowed because auto-checkout failed: " + e.getMessage());
        }

        reservation.setStatus(ReservationStatus.BORROWED);
        reservation.setFulfilledAt(LocalDateTime.now());

        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationMapper.toDTO(savedReservation);
    }

    @Override
    public PageResponse<ReservationDTO> getMyReservations(ReservationSearchRequest request) {
        try {
            User user = userService.getUser();

            Pageable pageable = createPageable(
                    request.getPage(), request.getSize(),
                    request.getSortBy(), request.getSortDirection()
            );

            Page<Reservation> reservationPage;

            if (request.getStatus() != null) {
                reservationPage = reservationRepository.findByUserIdAndStatus(
                        user.getId(), request.getStatus(), pageable
                );
            } else if (Boolean.TRUE.equals(request.getActiveOnly())) {
                List<ReservationStatus> activeStatuses = Arrays.asList(
                        ReservationStatus.PENDING, ReservationStatus.READY, ReservationStatus.USER_CONFIRMED
                );
                reservationPage = reservationRepository.findByUserIdAndStatusIn(
                        user.getId(), activeStatuses, pageable
                );
            } else {
                reservationPage = reservationRepository.findByUserId(user.getId(), pageable);
            }

            return convertToPageResponse(reservationPage);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PageResponse<ReservationDTO> searchReservations(ReservationSearchRequest request) {
        Pageable pageable = createPageable(
                request.getPage(), request.getSize(),
                request.getSortBy(), request.getSortDirection()
        );

        Page<Reservation> reservationPage;

        if (request.getUserId() != null && request.getStatus() != null) {
            reservationPage = reservationRepository.findByUserIdAndStatus(
                    request.getUserId(), request.getStatus(), pageable
            );
        } else if (request.getUserId() != null) {
            reservationPage = reservationRepository.findByUserId(request.getUserId(), pageable);
        } else if (request.getBookId() != null) {
            reservationPage = reservationRepository.findByBookId(request.getBookId(), pageable);
        } else if (request.getStatus() != null) {
            reservationPage = reservationRepository.findByStatus(request.getStatus(), pageable);
        } else {
            reservationPage = reservationRepository.findAll(pageable);
        }

        return convertToPageResponse(reservationPage);
    }


    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        size = Math.min(100, size);
        size = Math.max(1, size);

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return PageRequest.of(page, size, sort);
    }

    private PageResponse<ReservationDTO> convertToPageResponse(Page<Reservation> reservationPage) {
        List<ReservationDTO> reservationDTOS = reservationPage.getContent()
                .stream()
                .map(reservationMapper::toDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                reservationDTOS,
                reservationPage.getNumber(),
                reservationPage.getSize(),
                reservationPage.getTotalElements(),
                reservationPage.getTotalPages(),
                reservationPage.isLast(),
                reservationPage.isFirst(),
                reservationPage.isEmpty()
        );
    }
}
