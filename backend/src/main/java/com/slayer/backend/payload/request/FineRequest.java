package com.slayer.backend.payload.request;


import com.slayer.backend.Enums.FineType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FineRequest {

    @NotNull(message = "Book loan id is required")
    private Long bookLoanId;

    @NotNull(message = "Fine type is required")
    private FineType type;

    @NotNull(message = "Fine amount is required")
    @Positive(message = "Fine amount must be positive")
    private Long amount;

    private String reason;

    private String note;

}
