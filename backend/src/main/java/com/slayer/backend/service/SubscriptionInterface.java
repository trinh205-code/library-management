package com.slayer.backend.service;

import com.slayer.backend.exception.SubscriptionException;
import com.slayer.backend.payload.dto.SubscriptionDTO;
import com.slayer.backend.payload.reponse.PaymentInitiateResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SubscriptionInterface {

    PaymentInitiateResponse subscribe(SubscriptionDTO subscriptionDTO) throws Exception;
    SubscriptionDTO getUsersActiveSubscription(Long userId) throws Exception;

    SubscriptionDTO cancelSubscription(Long subscriptionId, String reason) throws SubscriptionException;

    SubscriptionDTO activeSubscription(Long subscriptionId, Long paymentId) throws SubscriptionException;

    List<SubscriptionDTO> getAllSubscriptions(Pageable pageable);

    void deactivateExpireSubscriptions() throws SubscriptionException;



}
