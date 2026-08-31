package com.slayer.backend.payload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionPlanDTO {

    private Long id;

    @NotBlank(message = "plan code is required")
    private String planCode;

    @NotBlank(message = "name is required")
    private String name;

    private String description;

    @NotNull(message = "durationDays is required")
    @Positive(message = "durationDays is positive")
    private Integer durationDays;

    @NotNull(message = "price is required")
    @Positive(message = "price is positive")
    private Long price;

    private String currency;

    @NotNull(message = "Max book is required")
    @Positive(message = "Max book must be positive")
    private Integer maxBooksAllowed;

    @NotNull(message = "Max day is required")
    @Positive(message = "Max day must be positive")
    private Integer maxDaysPerBook;

    private Integer displayOrder;
    private Boolean isActive;
    private Boolean isFeatured;
    private String badgeText;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

}
