package com.slayer.backend.repository;

import com.slayer.backend.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {

    @Query("select s from Subscription s where s.user.id = :userId AND " +
            "s.isActive = true AND " +
            "s.startDate<=:today AND s.endDate>=:today ORDER BY s.id DESC"
    )
    List<Subscription> findActiveSubscriptionByUserId(
            @Param("userId")Long userId,
            @Param("today") LocalDate today
    );

    @Query("select s from Subscription s where s.isActive=true " +
            "AND s.endDate<:today")
    List<Subscription> findExpireActiveSubscriptions(
            @Param("today")LocalDate today
    );

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.isActive = true AND s.endDate >= :today")
    Long countActiveSubscriptions(@Param("today") LocalDate today);

    @Query("SELECT s.planName FROM Subscription s GROUP BY s.planName ORDER BY COUNT(s) DESC")
    List<String> findTopSellingPlans(org.springframework.data.domain.Pageable pageable);
}

