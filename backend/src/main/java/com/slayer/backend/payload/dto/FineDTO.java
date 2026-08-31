package com.slayer.backend.payload.dto;

import com.slayer.backend.Enums.FineStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FineDTO {
    private Long id;

    @NotNull
    private Long bookLoanId;

    private String bookTitle;

    private String bookIsbn;

    @NotNull(message = "User Id is required")
    private Long userId;

    private String userName;

    private String userEmail;

    @PositiveOrZero(message = "Fine amount cannot be negative")
    private Long amount;

    @PositiveOrZero(message = "Amount paid cannot be negative")
    private Long amountPaid;

    private Long amountOutstanding;

    @NotNull(message = "Fine status is required")
    private FineStatus status;

    private String reason;

    private String note;

    private Long waivedByUserId;

    private String waivedByUserName;

    private LocalDateTime waivedAt;

    private String waiverReason;

    private LocalDateTime paidAt;

    private Long processedByUserName;

    private String transactionId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
