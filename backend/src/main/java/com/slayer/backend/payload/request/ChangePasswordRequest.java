package com.slayer.backend.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotNull(message = "newPassword is required")
    private String newPassword;

    @NotNull(message = "otp is required")
    private String otp;
}
