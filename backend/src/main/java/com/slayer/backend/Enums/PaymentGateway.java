package com.slayer.backend.Enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PaymentGateway {
    VNPAY,
    RAZORPAY,
    MOMO;

    @JsonCreator
    public static PaymentStatus from(String value) {
        try {
            return PaymentStatus.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Status must be one of: PENDING, SUCCESS, FAILED"
            );
        }
    }

}
