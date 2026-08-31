package com.slayer.backend.Enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PaymentType {
    FINE,
    MEMBERSHIP,
    LOST_BOOK_PENALTY,
    DAMAGED_BOOK_PENALTY;

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
