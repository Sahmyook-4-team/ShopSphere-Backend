package com.shopsphere.shopsphere_web.controller;

import com.shopsphere.shopsphere_web.dto.UserDTO;
import com.shopsphere.shopsphere_web.dto.UserDTO;
import com.shopsphere.shopsphere_web.entity.User;
import com.shopsphere.shopsphere_web.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO.LoginRequest userDTO) {
        User user = userService.authenticate(userDTO.getId(), userDTO.getPassword());
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(401).body("{\"message\": \"Invalid ID or password\"}");
    }

    @GetMapping("/oauth/kakao/callback")
    public ResponseEntity<?> kakaoLoginCallback(@RequestParam("code") String code) {
        try {
            // 1. 인가 코드로 카카오 Access Token 요청
            String accessToken = userService.getKakaoAccessToken(code);
            
            // 2. 카카오 사용자 정보 조회
            User user = userService.processKakaoLogin(accessToken);
            
            if (user != null) {
                return ResponseEntity.ok(user);
            }
            return ResponseEntity.status(401).body("{\"message\": \"Kakao login failed\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
