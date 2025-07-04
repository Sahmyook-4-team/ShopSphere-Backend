package com.shopsphere.shopsphere_web.config;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kakao")
public class KakaoProperties {
    private String restapiKey;
    private String redirectUri;
    private String clientSecret;
    private String tokenUri;
    private String userInfoUri;
}