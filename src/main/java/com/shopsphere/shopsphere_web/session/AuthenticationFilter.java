package com.shopsphere.shopsphere_web.jwtutil;

import com.shopsphere.shopsphere_web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private UserService userService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        // 1. 일반 로그인 헤더 확인
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            // Basic 인증 처리
            String credentials = new String(java.util.Base64.getDecoder().decode(authHeader.substring(6)));
            String[] parts = credentials.split(":");
            String id = parts[0];
            String password = parts[1];
            
            try {
                User user = userService.authenticate(id, password);
                if (user != null) {
                    processAuthentication(request, response, user);
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        
        // 2. 카카오 로그인 헤더 확인 이건 건들지 마쇼
        String kakaoHeader = request.getHeader("X-Kakao-Auth");
        if (kakaoHeader != null && kakaoHeader.startsWith("Kakao ")) {
            String accessToken = kakaoHeader.substring(6);
            
            try {
                User user = userService.processKakaoLogin(accessToken);
                if (user != null) {
                    processAuthentication(request, response, user);
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
    
    private void processAuthentication(HttpServletRequest request, HttpServletResponse response, User user) {
        // 세션에 사용자 정보 저장
        HttpSession session = request.getSession();
        session.setAttribute("user", user);
        
        // 인증 처리
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                user, 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("USER"))
            );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
