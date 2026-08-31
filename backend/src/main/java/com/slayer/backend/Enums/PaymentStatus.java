package com.slayer.backend.Enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    CANCELED,
    REFUNDED,
    PROCESSING;


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
