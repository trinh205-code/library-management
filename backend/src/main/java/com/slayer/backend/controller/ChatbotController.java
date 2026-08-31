package com.slayer.backend.controller;

import com.slayer.backend.dto.ChatbotRequest;
import com.slayer.backend.dto.ChatbotResponse;
import com.slayer.backend.entity.User;
import com.slayer.backend.service.ChatbotService;
import com.slayer.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final UserRepository userRepository;

    @PostMapping("/ask")
    public ResponseEntity<ChatbotResponse> askChatbot(
            @RequestBody ChatbotRequest request,
            Authentication authentication) {
        
        User user = null;
        if (authentication != null && authentication.getName() != null) {
            try {
                user = userRepository.findByEmail(authentication.getName()).orElse(null);
            } catch (Exception e) {
                // Ignore
            }
        }

        ChatbotResponse response = chatbotService.processChat(request, user);
        return ResponseEntity.ok(response);
    }
}
