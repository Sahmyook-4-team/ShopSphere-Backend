package com.shopsphere.shopsphere_web.controller;

import com.shopsphere.shopsphere_web.dto.chatbot.ChatRequest;
import com.shopsphere.shopsphere_web.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        try {
            String response = chatbotService.getChatResponse(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("챗봇 응답을 가져오는 중 오류가 발생했습니다.");
        }
    }
}
