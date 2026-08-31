package com.slayer.backend.service;

import com.slayer.backend.entity.User;
import com.slayer.backend.payload.dto.PaymentDTO;
import com.slayer.backend.payload.reponse.PaymentInitiateResponse;
import com.slayer.backend.payload.request.PaymentInitiateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentServiceInterface {

    PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request, User user);

    PaymentDTO verifyPayment(PaymentInitiateRequest request);

    Page<PaymentDTO> getAllPayments(com.slayer.backend.Enums.PaymentStatus status,
                                    com.slayer.backend.Enums.PaymentType type,
                                    String userName,
                                    java.time.LocalDateTime startDate,
                                    java.time.LocalDateTime endDate,
                                    Pageable pageable);

    Page<PaymentDTO> getMyPayments(Long userId, Pageable pageable);

    void processPaymentSuccess(String transactionId);
}
