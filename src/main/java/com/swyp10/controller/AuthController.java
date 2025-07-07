package com.swyp10.controller;

import com.swyp10.dto.auth.common.LoginRequest;
import com.swyp10.dto.auth.common.SignupRequest;
import com.swyp10.dto.auth.common.TokenResponse;
import com.swyp10.service.auth.common.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
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
     * 추가 회원가입 완료 - 토큰에서 OAuth 계정 ID 추출
     */
    @PostMapping("/oauth/signup")
    @Operation(
        summary = "추가 회원가입 완료", 
        description = "OAuth 토큰을 통해 추가 회원가입 완료",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<TokenResponse> completeAdditionalSignup(
        @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader,
        @Parameter(description = "추가 회원가입 정보") @Valid @RequestBody SignupRequest request
    ) {
        TokenResponse tokenResponse = authService.completeAdditionalSignup(authHeader, request);
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
    public ResponseEntity<AuthService.UserInfo> getCurrentUser(
        @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        AuthService.UserInfo userInfo = authService.getCurrentUser(authHeader);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 이메일 회원가입
     */
    @PostMapping("/signup")
    @Operation(summary = "이메일 회원가입", description = "이메일을 통한 회원가입")
    public ResponseEntity<TokenResponse> signup(@RequestBody @Valid SignupRequest request) {
        TokenResponse token = authService.signup(request);
        return ResponseEntity.ok(token);
    }

    /**
     * 이메일 로그인
     */
    @PostMapping("/login")
    @Operation(summary = "이메일 로그인", description = "이메일과 비밀번호로 로그인")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        TokenResponse token = authService.login(request);
        return ResponseEntity.ok(token);
    }

    /**
     * 이메일 중복 확인
     */
    @GetMapping("/check-email")
    @Operation(summary = "이메일 중복 확인", description = "이메일 사용 가능 여부 확인")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean available = authService.checkEmailAvailable(email);
        return ResponseEntity.ok(available);
    }
}
