package com.swyp10.service.auth.common;

import com.swyp10.dto.auth.common.LoginRequest;
import com.swyp10.dto.auth.common.SignupRequest;
import com.swyp10.dto.auth.common.TokenResponse;
import com.swyp10.entity.LoginType;
import com.swyp10.entity.User;
import com.swyp10.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailAuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    
    /**
     * 이메일 회원가입
     */
    @Transactional
    public TokenResponse signup(SignupRequest request) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다");
        }
        
        // 사용자 생성
        User newUser = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .nickname(request.getNickname())
            .loginType(LoginType.EMAIL)
            .signupCompleted(true)
            .build();
        
        User savedUser = userRepository.save(newUser);
        
        // JWT 토큰 생성
        String accessToken = jwtTokenService.generateAccessToken(savedUser);
        
        log.info("이메일 회원가입 완료: userId={}, email={}", savedUser.getUserId(), request.getEmail());
        
        return TokenResponse.of(accessToken, savedUser.getUserId(), savedUser.getNickname());
    }
    
    /**
     * 이메일 로그인
     */
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("등록되지 않은 이메일입니다"));
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }
        
        // JWT 토큰 생성
        String accessToken = jwtTokenService.generateAccessToken(user);
        
        log.info("이메일 로그인 성공: userId={}, email={}", user.getUserId(), request.getEmail());
        
        return TokenResponse.of(accessToken, user.getUserId(), user.getNickname());
    }
    
    /**
     * 이메일 중복 확인
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}
