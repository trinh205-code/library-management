package com.slayer.backend.payload.dto;

import com.slayer.backend.Enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    private Long id;
    @NotNull(message = "email is required")
    private String email;

    @NotNull(message = "fullName id required")
    private String fullName;
    private String phone;
    private UserRole role;

    @NotNull(message = "password is required")
    private String password;
    private LocalDateTime lastLogin;

}
