package com.slayer.backend.event;

import com.slayer.backend.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishPaymentSuccessEvent(Payment payment) {
        applicationEventPublisher.publishEvent(new PaymentSuccessEvent(payment));
    }
}
