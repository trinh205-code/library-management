package com.slayer.backend.service.impl;

import com.slayer.backend.entity.User;
import com.slayer.backend.mapper.UserMapper;
import com.slayer.backend.payload.dto.UserDTO;
import com.slayer.backend.payload.request.ChangePasswordRequest;
import com.slayer.backend.payload.request.UpdateProfileRequest;
import com.slayer.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EmailServiceImpl emailService;
    private final PasswordEncoder passwordEncoder;



    public User getUser() throws Exception {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                ()-> new Exception("user not found")
        );

        return user;
    }

    public void generateAndSendProfileUpdateOtp() throws Exception {
        User user = getUser();
        
        // Generate 6-digit OTP
        Random random = new Random();
        int otpValue = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpValue);
        
        user.setProfileUpdateOtp(otp);
        user.setProfileUpdateOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
        
        String subject = "Password Change Verification Code";
        String body = "<div style=\"font-family: Arial, sans-serif; padding: 20px;\">" +
                      "<h2>Password Change</h2>" +
                      "<p>Your OTP for changing your password is:</p>" +
                      "<h1 style=\"color: #4a90e2; letter-spacing: 2px;\">" + otp + "</h1>" +
                      "<p>This code is valid for 5 minutes. If you did not request this, please ignore this email.</p>" +
                      "</div>";
                      
        emailService.sendMail(user.getEmail(), subject, body);
    }

    public User updateUserProfile(UpdateProfileRequest request) throws Exception {
        User user = getUser();
        
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        
        return userRepository.save(user);
    }

    public User changePassword(ChangePasswordRequest request) throws Exception {
        User user = getUser();
        
        if (request.getOtp() == null || request.getOtp().isEmpty()) {
            throw new Exception("OTP is required");
        }
        
        if (user.getProfileUpdateOtp() == null || !user.getProfileUpdateOtp().equals(request.getOtp())) {
            throw new Exception("Invalid OTP");
        }
        
        if (user.getProfileUpdateOtpExpiry() == null || user.getProfileUpdateOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new Exception("OTP has expired");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        
        // Clear OTP after successful update
        user.setProfileUpdateOtp(null);
        user.setProfileUpdateOtpExpiry(null);
        
        return userRepository.save(user);
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(
                userMapper::toDTO
        ).collect(Collectors.toList());
    }

    public User findById(Long id) throws Exception {
        return userRepository.findById(id).orElseThrow(
                ()-> new Exception("user not found")
        );
    }

    public User adminUpdateUser(Long id, com.slayer.backend.payload.request.AdminUpdateUserRequest request) throws Exception {
        User user = findById(id);
        
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        
        return userRepository.save(user);
    }

    public void adminDeleteUser(Long id) throws Exception {
        User user = findById(id);
        userRepository.delete(user);
    }


}
