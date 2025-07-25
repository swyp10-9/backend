package com.swyp10.util;

import com.swyp10.domain.auth.entity.User;
import com.swyp10.domain.auth.dto.common.TokenResponse;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * 인증 관련 응답 생성 유틸리티
 */
public class AuthResponseUtil {
    
    /**
     * 성공 응답 생성
     */
    public static ResponseEntity<?> success(Object data) {
        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "data", data
        ));
    }
    
    /**
     * 성공 응답 생성 (메시지 포함)
     */
    public static ResponseEntity<?> success(Object data, String message) {
        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "data", data,
            "message", message
        ));
    }
    
    /**
     * 에러 응답 생성
     */
    public static ResponseEntity<?> error(String message) {
        return ResponseEntity.badRequest().body(Map.of(
            "status", "ERROR",
            "message", message
        ));
    }
    
    /**
     * 서버 에러 응답 생성
     */
    public static ResponseEntity<?> serverError(String message) {
        return ResponseEntity.status(500).body(Map.of(
            "status", "ERROR",
            "message", message
        ));
    }
    
    /**
     * 인증 에러 응답 생성
     */
    public static ResponseEntity<?> unauthorized(String message) {
        return ResponseEntity.status(401).body(Map.of(
            "status", "ERROR",
            "message", message
        ));
    }
    
    /**
     * OAuth 로그인 성공 응답
     */
    public static ResponseEntity<?> loginSuccess(TokenResponse tokenResponse) {
        return ResponseEntity.ok(Map.of(
            "status", "LOGIN_SUCCESS",
            "data", tokenResponse
        ));
    }
    
    /**
     * 추가 회원가입 필요 응답
     */
    public static ResponseEntity<?> additionalSignupRequired(Long oauthAccountId) {
        return ResponseEntity.ok(Map.of(
            "status", "ADDITIONAL_SIGNUP_REQUIRED",
            "oauthAccountId", oauthAccountId,
            "message", "추가 회원가입이 필요합니다."
        ));
    }
    
    /**
     * 회원가입 성공 응답
     */
    public static ResponseEntity<?> signupSuccess(TokenResponse tokenResponse) {
        return ResponseEntity.ok(Map.of(
            "status", "SIGNUP_SUCCESS",
            "data", tokenResponse,
            "message", "회원가입이 완료되었습니다."
        ));
    }
    
    /**
     * 사용자 정보 응답 객체 생성
     */
    public static Map<String, Object> createUserResponse(User user) {
        return Map.of(
            "userId", user.getUserId(),
            "email", user.getEmail(),
            "nickname", user.getNickname(),
            "profileImage", user.getProfileImage() != null ? user.getProfileImage() : "",
            "signupCompleted", user.getSignupCompleted(),
            "createdAt", user.getCreatedAt(),
            "updatedAt", user.getUpdatedAt()
        );
    }
}
