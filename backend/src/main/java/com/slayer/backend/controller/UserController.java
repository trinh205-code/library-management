package com.slayer.backend.controller;

import com.slayer.backend.entity.User;
import com.slayer.backend.payload.dto.UserDTO;
import com.slayer.backend.service.impl.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.slayer.backend.payload.request.ChangePasswordRequest;
import com.slayer.backend.payload.request.UpdateProfileRequest;
import com.slayer.backend.payload.request.AdminUpdateUserRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")

public class UserController {
    private final UserService userService;

    @GetMapping("/list")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }


    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile() throws Exception {

        return ResponseEntity.ok(userService.getUser());
    }

    @PostMapping("/profile/send-otp")
    public ResponseEntity<String> sendProfileUpdateOtp() throws Exception {
        userService.generateAndSendProfileUpdateOtp();
        return ResponseEntity.ok("OTP sent successfully");
    }

    @PutMapping("/profile/update")
    public ResponseEntity<User> updateProfile(@RequestBody UpdateProfileRequest request) throws Exception {
        return ResponseEntity.ok(userService.updateUserProfile(request));
    }

    @PutMapping("/profile/change-password")
    public ResponseEntity<User> changePassword(@RequestBody ChangePasswordRequest request) throws Exception {
        return ResponseEntity.ok(userService.changePassword(request));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<User> adminUpdateUser(@PathVariable Long id, @RequestBody AdminUpdateUserRequest request) throws Exception {
        return ResponseEntity.ok(userService.adminUpdateUser(id, request));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<String> adminDeleteUser(@PathVariable Long id) throws Exception {
        userService.adminDeleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}
