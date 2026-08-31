package com.slayer.backend.payload.request;

import com.slayer.backend.Enums.UserRole;
import lombok.Data;

@Data
public class AdminUpdateUserRequest {
    private String fullName;
    private String phone;
    private UserRole role;
}
