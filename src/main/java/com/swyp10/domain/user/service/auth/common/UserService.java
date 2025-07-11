package com.swyp10.domain.user.service.auth.common;

import com.swyp10.domain.user.dto.auth.common.SignupRequest;
import com.swyp10.domain.user.entity.LoginType;
import com.swyp10.domain.user.entity.OAuthAccount;
import com.swyp10.domain.user.entity.User;
import com.swyp10.global.exception.ApplicationException;
import com.swyp10.global.exception.ErrorCode;
import com.swyp10.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관리 통합 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    
    // === 사용자 조회 ===
    
    /**
     * 사용자 ID로 조회
     */
    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
    }
    
    /**
     * 이메일로 사용자 조회
     */
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
    }
    
    // === 이메일 관련 ===
    
    /**
     * 이메일 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
    
    /**
     * 이메일 중복 검증 (예외 발생)
     */
    public void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ApplicationException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
    
    // === 회원가입 ===
    
    /**
     * 이메일 회원가입
     */
    @Transactional
    public User createEmailUser(SignupRequest request) {
        // 이메일 중복 검증
        validateEmailNotExists(request.getEmail());
        
        User newUser = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .nickname(request.getNickname())
            .loginType(LoginType.EMAIL)
            .signupCompleted(true)
            .build();
        
        User savedUser = userRepository.save(newUser);
        
        log.info("이메일 회원가입 완료: userId={}, email={}", 
                savedUser.getUserId(), request.getEmail());
        
        return savedUser;
    }
    
    /**
     * OAuth 추가 회원가입 완료
     */
    @Transactional
    public User completeOAuthSignup(Long oauthAccountId, SignupRequest request) {
        // OAuth 계정 조회
        OAuthAccount oauthAccount = accountService.findById(oauthAccountId);
        
        // 이메일 중복 검증
        validateEmailNotExists(request.getEmail());
        
        // 새 User 생성
        User newUser = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .nickname(request.getNickname())
            .profileImage(oauthAccount.getProviderProfileImage())
            .loginType(oauthAccount.getProvider())
            .signupCompleted(true)
            .build();
        
        User savedUser = userRepository.save(newUser);
        
        // OAuth 계정과 User 연결
        accountService.linkWithUser(oauthAccountId, savedUser);
        
        log.info("OAuth 추가 회원가입 완료: userId={}, oauthAccountId={}, email={}", 
                savedUser.getUserId(), oauthAccountId, request.getEmail());
        
        return savedUser;
    }
    
    // === Private 메서드들 ===
    
    /**
     * 사용자 저장
     */
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
}
