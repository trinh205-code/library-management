package com.slayer.backend.payload.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDTO {

    private Long id;

    @NotNull(message = "User Id is Required")
    private Long userId;

    private String userName;
    private String userEmail;

    @NotNull(message = "Subscription plan Id is Required")
    private Long planId;

    private String planName;
    private String planCode;
    private Long price;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private Integer maxDaysPerBook;
    private Integer maxBooksAllowed;
    private Boolean autoRenew;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private String notes;
    private Long daysRemaining;
    private Boolean isValid;
    private Boolean isExpired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
