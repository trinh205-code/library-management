package com.slayer.backend.event;

import com.slayer.backend.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentSuccessEvent {
    private Payment payment;
}