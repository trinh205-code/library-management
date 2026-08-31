package com.slayer.backend.payload.request;

import com.slayer.backend.Enums.BookLoanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckinRequest {

    @NotNull(message = "book id is required")
    private Long bookLoanId;
    private BookLoanStatus condition = BookLoanStatus.RETURNED;
    private String notes;

}
