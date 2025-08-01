package com.swyp10.domain.auth.controller;

import com.swyp10.domain.auth.dto.common.SignupRequest;
import com.swyp10.domain.auth.dto.common.UserInfo;
import com.swyp10.domain.auth.service.common.AuthService;
import com.swyp10.domain.auth.dto.common.LoginRequest;
import com.swyp10.domain.auth.dto.common.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "OAuth 로그인 및 회원가입 API")
public class AuthController {
    
    private final AuthService authService;

    /**
     * OAuth 인가 코드로 로그인 - 항상 토큰 반환
     */
    @PostMapping("/oauth/login/{provider}")
    @Operation(summary = "OAuth 인가 코드 로그인", description = "OAuth 인가 코드로 로그인 처리 - 항상 토큰 반환")
    public ResponseEntity<TokenResponse> oauthLogin(
        @Parameter(description = "OAuth 제공자", example = "kakao") @PathVariable String provider,
        @Parameter(description = "OAuth 인가 코드") @RequestParam String code
    ) {
        TokenResponse tokenResponse = authService.processOAuthLogin(provider, code);
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * JWT 토큰으로 사용자 정보 조회
     */
    @GetMapping("/me")
    @Operation(
        summary = "사용자 정보 조회", 
        description = "JWT 토큰으로 현재 로그인한 사용자 정보 조회",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<UserInfo> getCurrentUser(
        @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        UserInfo userInfo = authService.getCurrentUser(authHeader);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 토큰 연장
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "토큰 연장", 
        description = "현재 토큰을 연장하여 새로운 토큰 발급",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<TokenResponse> refreshToken(
        @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        TokenResponse tokenResponse = authService.refreshToken(authHeader);
        return ResponseEntity.ok(tokenResponse);
    }
}
