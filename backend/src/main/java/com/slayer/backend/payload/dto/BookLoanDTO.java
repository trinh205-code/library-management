package com.slayer.backend.payload.dto;

import com.slayer.backend.Enums.BookLoanStatus;
import com.slayer.backend.Enums.BookLoanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookLoanDTO {

    private Long id;

    private Long userId;
    private String userName;
    private String userEmail;

    private String bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookIsbn;
    private String bookCoverImage;
    private BookLoanType type;
    private BookLoanStatus status;
    private LocalDateTime checkoutDate;
    private LocalDateTime dueDate;
    private Integer remainingDays;
    private LocalDateTime returnDate;

    private Integer renewalCount;

    private Integer maxRenewals;

    private Integer fineAmount;

    private Boolean finePaid;
    private String notes;

    private Boolean isOverdue;

    private Integer overdueDays;

    private Boolean hasReviewed;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;
}
