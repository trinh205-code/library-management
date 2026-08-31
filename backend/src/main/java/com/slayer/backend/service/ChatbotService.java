package com.slayer.backend.service;

import com.slayer.backend.dto.ChatbotRequest;
import com.slayer.backend.dto.ChatbotResponse;
import com.slayer.backend.entity.User;

public interface ChatbotService {
    ChatbotResponse processChat(ChatbotRequest request, User user);
}
