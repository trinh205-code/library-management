package com.slayer.backend.payload.reponse;

import com.slayer.backend.Enums.PaymentGateway;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentInitiateResponse {

    private  Long userId;
    private Long paymentId;
    private PaymentGateway paymentGateway;
    private String transactionId;


    private Long amount;
    private String description;

    private String checkoutUrl;
    private String message;
    private Boolean success;

}
