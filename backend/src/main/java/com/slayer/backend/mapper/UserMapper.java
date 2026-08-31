package com.slayer.backend.mapper;

import com.slayer.backend.entity.User;
import com.slayer.backend.payload.dto.UserDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class UserMapper {

    public UserDTO toDTO(User user) {
        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .password(user.getPassword())
                .role(user.getRole())
                .lastLogin(user.getLastLogin())
                .build();

        return userDTO;
    }


    public User toEntity(UserDTO userDTO) {
        User user = User.builder()
                .email(userDTO.getEmail())
                .fullName(userDTO.getFullName())
                .password(userDTO.getPassword())
                .phone(userDTO.getPhone())
                .role(userDTO.getRole())
                .createdAt(LocalDateTime.now())
                .build();

        return user;
    }

    public List<UserDTO> toDTOList(List<User> users) {

        return users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Set<UserDTO> toDTOSet(Set<User> users) {
        return users.stream()
                .map(this::toDTO)
                .collect(Collectors.toSet());
    }



}
