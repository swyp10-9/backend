package com.swyp10.controller;

import com.swyp10.dto.auth.OAuthProvider;
import com.swyp10.service.auth.common.*;
import com.swyp10.util.AuthResponseUtil;
import com.swyp10.dto.auth.common.LoginRequest;
import com.swyp10.dto.auth.common.SignupRequest;
import com.swyp10.dto.auth.common.TokenResponse;
import com.swyp10.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증", description = "OAuth 로그인 및 회원가입 API")
public class AuthController {
    
    private final OAuthService oauthService;
    private final JwtTokenService jwtTokenService;
    private final UserService userService;
    private final AuthTokenService authTokenService;
    private final EmailAuthService emailAuthService;

    /**
     * OAuth 인가 코드로 로그인 - 항상 토큰 반환
     */
    @PostMapping("/oauth/login/{provider}")
    @Operation(summary = "OAuth 인가 코드 로그인", description = "OAuth 인가 코드로 로그인 처리 - 항상 토큰 반환")
    public ResponseEntity<?> oauthLogin(
        @Parameter(description = "OAuth 제공자", example = "kakao") @PathVariable String provider,
        @Parameter(description = "OAuth 인가 코드") @RequestParam String code
    ) {
        try {
            OAuthProvider oauthProvider = OAuthProvider.fromString(provider);
            
            log.info("{} OAuth 로그인 요청: code={}", provider, code.substring(0, Math.min(code.length(), 10)) + "...");
            
            OAuthService.OAuthLoginResult result = oauthService.processOAuthLoginByCode(oauthProvider, code);
            
            if (result.isSignupCompleted()) {
                log.info("{} 로그인 성공 (완료): userId={}", provider, result.getTokenResponse().getUserId());
            } else {
                log.info("{} 로그인 성공 (추가 회원가입 필요)", provider);
            }
            
            return AuthResponseUtil.success(result.getTokenResponse());
            
        } catch (IllegalArgumentException e) {
            log.warn("지원하지 않는 OAuth 제공자: {}", provider);
            return AuthResponseUtil.error("지원하지 않는 OAuth 제공자입니다.");
            
        } catch (Exception e) {
            log.error("{} OAuth 로그인 실패: {}", provider, e.getMessage(), e);
            return AuthResponseUtil.serverError("로그인 처리 중 오류가 발생했습니다.");
        }
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
    public ResponseEntity<?> completeAdditionalSignup(
        @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader,
        @Parameter(description = "추가 회원가입 정보") @Valid @RequestBody SignupRequest request
    ) {
        try {
            // OAuth 토큰 검증 및 추출
            String token = authTokenService.extractAndValidateToken(authHeader);
            if (token == null) {
                return AuthResponseUtil.unauthorized("유효하지 않은 인증 토큰입니다.");
            }
            
            // 토큰이 OAuth 타입인지 확인
            String tokenType = jwtTokenService.getTokenType(token);
            if (!"OAUTH".equals(tokenType)) {
                return AuthResponseUtil.error("OAuth 토큰이 필요합니다.");
            }
            
            // 토큰에서 OAuth 계정 ID 추출
            Long oauthAccountId = jwtTokenService.getOAuthAccountIdFromToken(token);
            
            log.info("추가 회원가입 요청: oauthAccountId={}, email={}", oauthAccountId, request.getEmail());
            
            // 추가 회원가입 완료
            User user = userService.completeAdditionalSignup(oauthAccountId, request);
            
            // 완전한 USER 토큰 생성
            String accessToken = jwtTokenService.generateAccessToken(user);
            TokenResponse tokenResponse = TokenResponse.of(accessToken, user.getUserId(), user.getNickname());
            
            log.info("추가 회원가입 완료: userId={}, email={}", user.getUserId(), request.getEmail());
            
            return AuthResponseUtil.success(tokenResponse, "회원가입이 완료되었습니다.");
            
        } catch (RuntimeException e) {
            log.warn("추가 회원가입 실패: error={}", e.getMessage());
            return AuthResponseUtil.error(e.getMessage());
            
        } catch (Exception e) {
            log.error("추가 회원가입 처리 중 오류: error={}", e.getMessage(), e);
            return AuthResponseUtil.serverError("회원가입 처리 중 오류가 발생했습니다.");
        }
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
    public ResponseEntity<?> getCurrentUser(
        @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            String token = authTokenService.extractAndValidateToken(authHeader);
            if (token == null) {
                return AuthResponseUtil.unauthorized("유효하지 않은 인증 토큰입니다.");
            }

            // 토큰 타입 확인
            String tokenType = jwtTokenService.getTokenType(token);

            if ("OAUTH".equals(tokenType)) {
                // OAuth 토큰인 경우 - 추가 회원가입 필요
                return AuthResponseUtil.error("추가 회원가입이 필요합니다.");
            } else if ("USER".equals(tokenType)) {
                // 완전한 사용자 토큰인 경우
                Long userId = jwtTokenService.getUserIdFromToken(token);
                User user = userService.getUserById(userId);

                log.info("사용자 정보 조회: userId={}", userId);

                return AuthResponseUtil.success(AuthResponseUtil.createUserResponse(user));
            } else {
                return AuthResponseUtil.unauthorized("알 수 없는 토큰 타입입니다.");
            }

        } catch (Exception e) {
            log.error("사용자 정보 조회 실패: {}", e.getMessage());
            return AuthResponseUtil.serverError("서버 오류가 발생했습니다.");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(@RequestBody @Valid SignupRequest request) {
        TokenResponse token = emailAuthService.signup(request);
        return ResponseEntity.ok(token);
    }

    /** 로그인 */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        TokenResponse token = emailAuthService.login(request);
        return ResponseEntity.ok(token);
    }

    /** 이메일 중복 확인 */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean available = emailAuthService.isEmailAvailable(email);
        return ResponseEntity.ok(available);
    }
}
