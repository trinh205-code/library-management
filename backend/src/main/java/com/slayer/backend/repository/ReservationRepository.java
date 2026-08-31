package com.slayer.backend.repository;

import com.slayer.backend.Enums.ReservationStatus;
import com.slayer.backend.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    Page<Reservation> findByBookId(Long bookId, Pageable pageable);

    Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);

    Page<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status, Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId AND r.status IN :statuses")
    Page<Reservation> findByUserIdAndStatusIn(@Param("userId") Long userId,
                                              @Param("statuses") List<ReservationStatus> statuses,
                                              Pageable pageable);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.user.id = :userId AND r.book.id = :bookId AND r.status IN :statuses")
    long countByUserIdAndBookIdAndStatusIn(@Param("userId") Long userId,
                                           @Param("bookId") Long bookId,
                                           @Param("statuses") List<ReservationStatus> statuses);

    @Query("SELECT COALESCE(MAX(r.queuePosition), 0) FROM Reservation r WHERE r.book.id = :bookId AND r.status = :status")
    int findMaxQueuePositionByBookIdAndStatus(@Param("bookId") Long bookId,
                                              @Param("status") ReservationStatus status);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.book.id = :bookId AND r.status = com.slayer.backend.Enums.ReservationStatus.PENDING")
    long countPendingReservationsByBookId(@Param("bookId") Long bookId);
}
