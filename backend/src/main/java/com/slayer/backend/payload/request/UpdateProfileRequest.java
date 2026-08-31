package com.slayer.backend.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotNull(message = "fullName is required")
    private String fullName;

    private String phone;
}
