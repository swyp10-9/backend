package com.swyp10.domain.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/test")
@Slf4j
public class OAuthTestController {
    
    @Value("${oauth.kakao.client-id}")
    private String KAKAO_CLIENT_ID;
    
    @Value("${oauth.kakao.redirect-uri}")
    private String KAKAO_REDIRECT_URI;
    
    /**
     * OAuth 테스트 페이지
     */
    @GetMapping("/oauth")
    public String oauthTestPage(Model model) {
        model.addAttribute("clientId", KAKAO_CLIENT_ID);
        model.addAttribute("redirectUri", KAKAO_REDIRECT_URI);
        return "oauth-test"; // oauth-test.html 템플릿 사용
    }
    
    /**
     * 카카오 인증 시작
     */
    @GetMapping("/kakao/auth")
    public RedirectView startKakaoAuth() {
        String kakaoAuthUrl = String.format(
            "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
            KAKAO_CLIENT_ID,
            KAKAO_REDIRECT_URI
        );
        
        log.info("카카오 인증 시작: {}", kakaoAuthUrl);
        return new RedirectView(kakaoAuthUrl);
    }
    
    /**
     * 카카오 콜백 (디버깅용)
     */
    @GetMapping("/kakao/callback")
    public String kakaoCallback(
        @RequestParam(required = false) String code,
        @RequestParam(required = false) String error,
        Model model
    ) {
        if (code != null) {
            log.info("✅ 카카오 인가 코드 받음: {}", code.substring(0, Math.min(code.length(), 20)) + "...");
            model.addAttribute("code", code);
            model.addAttribute("success", true);
            
            // curl 명령어 생성
            String curlCommand = String.format(
                "curl -X POST \"http://localhost:8080/api/auth/oauth/kakao/callback?code=%s\"", 
                code
            );
            model.addAttribute("curlCommand", curlCommand);
            
        } else if (error != null) {
            log.error("❌ 카카오 인증 실패: {}", error);
            model.addAttribute("error", error);
            model.addAttribute("success", false);
        }
        
        return "kakao-callback"; // 결과 페이지
    }
}
