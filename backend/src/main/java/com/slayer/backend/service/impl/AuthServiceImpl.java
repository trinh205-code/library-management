package com.slayer.backend.service.impl;

import com.slayer.backend.config.JwtProvider;
import com.slayer.backend.Enums.UserRole;
import com.slayer.backend.entity.PasswordResetToken;
import com.slayer.backend.entity.User;
import com.slayer.backend.exception.UserException;
import com.slayer.backend.mapper.UserMapper;
import com.slayer.backend.payload.dto.UserDTO;
import com.slayer.backend.payload.reponse.AuthResponse;
import com.slayer.backend.repository.PasswordResetTokenRepository;
import com.slayer.backend.repository.UserRepository;
import com.slayer.backend.service.AuthServiceInterface;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;
    private final CustomUserServiceImpl customUserServiceImpl;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailServiceImpl emailService;


    @Override
    public AuthResponse login(String userName, String password) throws UserException {
        Authentication auth = authenticate(userName, password);

        SecurityContextHolder.getContext().setAuthentication(auth);
//        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
//        String role = authorities.iterator().next().getAuthority();

        String token = jwtProvider.generateToken(auth);

        User user = userRepository.findByEmail(userName).orElseThrow(
                ()-> new RuntimeException("user not found")
        );

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        AuthResponse response = AuthResponse.builder()
                .title("Login success")
                .message("Welcome Back " + userName)
                .jwt(token)
                .userDTO(userMapper.toDTO(user))
                .build();


        return response;
    }

    private Authentication authenticate(String userName, String password) throws UserException {
        try {
            UserDetails userDetails = customUserServiceImpl.loadUserByUsername(userName);

            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                throw new UserException("Password does not match");
            }

            return new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

        } catch (UsernameNotFoundException e) {
            throw new UserException("User not found with email: " + userName);
        }
    }


    @Override
    public AuthResponse signup(UserDTO request) throws UserException {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserException("Email is already registered");
        }

        User createdUser = new User();
        createdUser.setEmail(request.getEmail());
        createdUser.setPassword(passwordEncoder.encode(request.getPassword()));
        createdUser.setPhone(request.getPhone());
        createdUser.setFullName(request.getFullName());
        createdUser.setLastLogin(LocalDateTime.now());
        createdUser.setRole(UserRole.ROLE_USER);

        User saveUser = userRepository.save(createdUser);


        Authentication auth = new UsernamePasswordAuthenticationToken(
                saveUser.getEmail(), saveUser.getPassword());

//        Authentication auth = new UsernamePasswordAuthenticationToken(
//                saveUser.getEmail(),
//                null,
//                List.of(new SimpleGrantedAuthority(saveUser.getRole().name()))
//        );

        SecurityContextHolder.getContext().setAuthentication(auth);


        String jwt = jwtProvider.generateToken(auth);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        authResponse.setTitle("Welcome " + request.getFullName());
        authResponse.setMessage("register success");
        authResponse.setUserDTO(userMapper.toDTO(saveUser));

        return authResponse;
    }

    @Override
    public void createPasswordResetToken(String email) throws UserException {

        String frontenUrl = "http://localhost:5173";

        User user = userRepository.findByEmail(email).orElseThrow(
                ()-> new RuntimeException("user not found with given email")
        );

        String token = String.valueOf(100000 + new java.util.Random().nextInt(900000));

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();

        passwordResetTokenRepository.save(resetToken);

        String subject = "Password Reset OTP Code";
        String body = "<div style=\"font-family: Arial, sans-serif; padding: 20px;\">" +
                      "<h2>Password Reset</h2>" +
                      "<p>Your OTP code to reset your password is:</p>" +
                      "<h1 style=\"color: #4a90e2; letter-spacing: 2px;\">" + token + "</h1>" +
                      "<p>This code is valid for 5 minutes. If you did not request this, please ignore this email.</p>" +
                      "</div>";

        emailService.sendMail(user.getEmail(), subject, body);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) throws Exception {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token).orElseThrow(
                ()-> new Exception("token not valid")
        );

        if(resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new Exception("token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
    }
}
