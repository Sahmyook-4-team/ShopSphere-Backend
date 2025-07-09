package com.shopsphere.shopsphere_web.dto.chatbot;

import lombok.Data;
import java.util.List;

@Data
public class ChatRequest {
    private String message;
    private String modelType;
    private List<ChatMessage> chatHistory;

    @Data
    public static class ChatMessage {
        private String role; // "user" or "assistant"
        private String content;
    }
}
