package com.slayer.backend.payload.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookReviewDTO {

    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String userFullName;

    @NotNull(message = "Book ID is required")
    private Long bookId;

    private String bookTitle;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @NotBlank(message = "Review text is required")
    @Size(min = 10, max = 2000, message = "Review text must be between 10 and 2000 characters")
    private String reviewText;

    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
