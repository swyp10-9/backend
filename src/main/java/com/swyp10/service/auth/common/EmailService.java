package com.swyp10.service.auth.common;

import com.swyp10.dto.auth.common.LoginRequest;
import com.swyp10.dto.auth.common.SignupRequest;
import com.swyp10.dto.auth.common.TokenResponse;
import com.swyp10.entity.User;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import com.swyp10.service.auth.token.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 이메일 회원가입
     */
    @Transactional
    public TokenResponse signup(SignupRequest request) {
        User newUser = userService.createEmailUser(request);
        
        // JWT 토큰 생성
        String accessToken = tokenService.generateAccessToken(newUser);
        
        return TokenResponse.of(accessToken, newUser.getUserId(), newUser.getNickname());
    }
    
    /**
     * 이메일 로그인
     */
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userService.findByEmail(request.getEmail());
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApplicationException(ErrorCode.INVALID_PASSWORD);
        }
        
        // JWT 토큰 생성
        String accessToken = tokenService.generateAccessToken(user);
        
        log.info("이메일 로그인 성공: userId={}, email={}", user.getUserId(), request.getEmail());
        
        return TokenResponse.of(accessToken, user.getUserId(), user.getNickname());
    }
    
    /**
     * 이메일 중복 확인
     */
    public boolean isEmailAvailable(String email) {
        return userService.isEmailAvailable(email);
    }
}
