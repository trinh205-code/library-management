package com.slayer.backend.payload.request;

import com.slayer.backend.Enums.PaymentGateway;
import com.slayer.backend.Enums.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentInitiateRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    private Long bookLoanId;

    private String transactionId;

    @NotNull(message = "payment type is required")
    private PaymentType paymentType;

    @NotNull(message = "payment gateway is required")
    private PaymentGateway paymentGateway;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be Positive")
    private Long amount;


    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Long fineId;

    private Long subscriptionId;

    @Size(max = 500, message = "Success URL must not exceed 500 characters")
    private String successUrl;

    @Size(max = 500, message = "Cancel URL must not exceed 500 characters")
    private String cancelUrl;
}
