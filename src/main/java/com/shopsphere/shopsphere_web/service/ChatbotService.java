package com.shopsphere.shopsphere_web.service;

import com.google.gson.Gson;
import com.shopsphere.shopsphere_web.config.ChatbotProperties;
import com.shopsphere.shopsphere_web.config.GeminiConfig;
import com.shopsphere.shopsphere_web.dto.chatbot.ChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final GeminiConfig geminiConfig;
    private final ChatbotProperties chatbotProperties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();

    public String getChatResponse(ChatRequest request) {
        String modelName = request.getModelType() != null ? request.getModelType() : "쇼핑몰";
        
        // 프로퍼티에서 모델 설정 가져오기
        ChatbotProperties.BotConfig botConfig = chatbotProperties.getBots().get(modelName);
        if (botConfig == null) {
            // 기본값으로 쇼핑몰 설정 사용
            botConfig = chatbotProperties.getBots().get("쇼핑몰");
        }
        
        String systemPrompt = botConfig.getSystemPrompt();

        try {
            // API 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(geminiConfig.getApiKey());

            // 대화 기록 구성
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 시스템 프롬프트 추가
            messages.add(Map.of("role", "user", 
                "content", systemPrompt + "\n\n이전 대화 기록을 바탕으로 다음 질문에 답변해주세요."));
            messages.add(Map.of("role", "model", "content", "네, 알겠습니다. 어떤 도움이 필요하신가요?"));
            
            // 이전 대화 기록 추가
            if (request.getChatHistory() != null && !request.getChatHistory().isEmpty()) {
                for (ChatRequest.ChatMessage msg : request.getChatHistory()) {
                    messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
                }
            }
            
            // 현재 메시지 추가
            messages.add(Map.of("role", "user", "content", request.getMessage()));

            // 요청 본문 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(Map.of("parts", List.of(Map.of("text", request.getMessage())))));
            
            // API 호출
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + geminiConfig.getApiKey();
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class);
            
            // 응답 처리
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("candidates")) {
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                    if (!candidates.isEmpty()) {
                        Map<String, Object> candidate = candidates.get(0);
                        if (candidate.containsKey("content")) {
                            Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                            if (content.containsKey("parts")) {
                                List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");
                                if (!parts.isEmpty()) {
                                    return parts.get(0).get("text");
                                }
                            }
                        }
                    }
                }
            }
            
            return "죄송합니다. 응답을 처리하는 중 오류가 발생했습니다.";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "죄송합니다. 챗봇 응답을 가져오는 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
}
