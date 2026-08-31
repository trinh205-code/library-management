package com.slayer.backend.controller;

import com.slayer.backend.exception.UserException;
import com.slayer.backend.payload.dto.UserDTO;
import com.slayer.backend.payload.reponse.ApiResponse;
import com.slayer.backend.payload.reponse.AuthResponse;
import com.slayer.backend.payload.request.ForgotPasswordRequest;
import com.slayer.backend.payload.request.LoginRequest;
import com.slayer.backend.payload.request.ResetPasswordRequest;
import com.slayer.backend.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);


    private final AuthServiceImpl authService;


    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signupHandler(@Valid @RequestBody  UserDTO request) throws UserException {

        AuthResponse res = authService.signup(request);

        return ResponseEntity.ok(res);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginHandler(@Valid @RequestBody  LoginRequest request) throws UserException {
        AuthResponse res = authService.login(request.getEmail(), request.getPassword());

        return ResponseEntity.ok(res);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) throws UserException {

        authService.createPasswordResetToken(request.getEmail());
        ApiResponse res = new ApiResponse("A Reset link was sent to your email", true);

        return ResponseEntity.ok(res);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordRequest request) throws Exception {

        authService.resetPassword(request.getToken(), request.getPassword());
        ApiResponse res = new ApiResponse("Password reset successful", true);
        return ResponseEntity.ok(res);
    }



}
