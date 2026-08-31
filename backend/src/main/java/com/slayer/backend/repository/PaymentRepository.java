package com.slayer.backend.repository;

import com.slayer.backend.entity.Payment;
import com.slayer.backend.Enums.PaymentStatus;
import com.slayer.backend.Enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByTransactionId(String transactionId);

    Page<Payment> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = :status")
    Long getTotalRevenue(@Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = :status AND p.paymentType = :type")
    Long getRevenueByType(@Param("status") PaymentStatus status, @Param("type") PaymentType type);

    @Query("SELECT YEAR(p.completedAt) as year, MONTH(p.completedAt) as month, SUM(p.amount) as total " +
           "FROM Payment p " +
           "WHERE p.paymentStatus = :status AND p.completedAt >= :startDate " +
           "GROUP BY YEAR(p.completedAt), MONTH(p.completedAt) " +
           "ORDER BY year, month")
    List<Object[]> getMonthlyRevenue(@Param("status") PaymentStatus status, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT YEAR(p.completedAt) as year, WEEK(p.completedAt) as week, SUM(p.amount) as total " +
           "FROM Payment p " +
           "WHERE p.paymentStatus = :status AND p.completedAt >= :startDate " +
           "GROUP BY YEAR(p.completedAt), WEEK(p.completedAt) " +
           "ORDER BY year, week")
    List<Object[]> getWeeklyRevenue(@Param("status") PaymentStatus status, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT YEAR(p.completedAt) as year, MONTH(p.completedAt) as month, DAY(p.completedAt) as day, SUM(p.amount) as total " +
           "FROM Payment p " +
           "WHERE p.paymentStatus = :status AND p.completedAt >= :startDate " +
           "GROUP BY YEAR(p.completedAt), MONTH(p.completedAt), DAY(p.completedAt) " +
           "ORDER BY year, month, day")
    List<Object[]> getDailyRevenue(@Param("status") PaymentStatus status, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT p FROM Payment p " +
           "WHERE (:status IS NULL OR p.paymentStatus = :status) " +
           "AND (:type IS NULL OR p.paymentType = :type) " +
           "AND (:userName IS NULL OR LOWER(p.user.fullName) LIKE LOWER(CONCAT('%', :userName, '%'))) " +
           "AND (p.createdAt >= :startDate) " +
           "AND (p.createdAt <= :endDate)")
    Page<Payment> findFilteredPayments(@Param("status") PaymentStatus status,
                                       @Param("type") PaymentType type,
                                       @Param("userName") String userName,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);
}
