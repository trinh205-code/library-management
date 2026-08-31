package com.slayer.backend.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RenewalRequest {

    @NotNull(message = "bookLoan id is required")
    private Long bookLoanId;

    @Min(value = 1, message = "extension day is must be least 1")
    private Integer extensionDays = 14;

    private String notes;
}
