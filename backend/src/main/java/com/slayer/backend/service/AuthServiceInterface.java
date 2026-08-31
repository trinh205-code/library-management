package com.slayer.backend.service;

import com.slayer.backend.exception.UserException;
import com.slayer.backend.payload.dto.UserDTO;
import com.slayer.backend.payload.reponse.AuthResponse;

public interface AuthServiceInterface {

    AuthResponse login(String userName, String password) throws UserException;
    AuthResponse signup(UserDTO request) throws UserException;

    void createPasswordResetToken(String email) throws UserException;
    void resetPassword(String token, String newPassword) throws Exception;

}
