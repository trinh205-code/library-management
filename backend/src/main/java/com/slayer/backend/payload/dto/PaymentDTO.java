package com.slayer.backend.payload.dto;

import com.slayer.backend.Enums.PaymentGateway;
import com.slayer.backend.Enums.PaymentStatus;
import com.slayer.backend.Enums.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
    @NotNull(message = "User ID is required")
    private Long userId;

    private String userName;

    private String userEmail;

    private Long bookLoanId;


//    private Long fineId;
    private Long subscriptionId;

    @NotNull(message = "payment type is required")
    private PaymentType paymentType;

    private PaymentStatus paymentStatus;

    @NotNull(message = "payment gateway is required")
    private PaymentGateway paymentGateway;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be Positive")
    private Long amount;


    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;


    @Size(max = 500, message = "Success URL must not exceed 500 characters")
    private String successUrl;

    @Size(max = 500, message = "Cancel URL must not exceed 500 characters")
    private String cancelUrl;


    private String transactionId;

    private String gatewayPaymentId;

    private String gatewayOrderId;

    private String gatewaySignature;

    private String paymentMethod;

    private String failureReason;

    private Integer retryCount;

    private LocalDateTime initiatedAt;

    private LocalDateTime completedAt;


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
