package com.shopsphere.shopsphere_web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "chatbot.bots")
public class ChatbotProperties {
    private Map<String, BotConfig> bots;

    @Data
    public static class BotConfig {
        private String modelName;
        private String systemPrompt;
    }
}
