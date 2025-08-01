package com.swyp10.domain.auth.service.common;

import com.swyp10.domain.auth.dto.common.SignupRequest;
import com.swyp10.domain.auth.entity.OAuthAccount;
import com.swyp10.domain.auth.entity.User;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import com.swyp10.domain.auth.repository.UserRepository;
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
    
    /**
     * 카카오 ID로 사용자 조회 또는 생성 (email 필드에 카카오 ID 저장)
     */
    @Transactional
    public User findByKakaoIdOrCreate(com.swyp10.domain.auth.dto.common.OAuthUserInfo oauthUserInfo) {
        // email 필드에 카카오 ID를 저장하라고 기존 사용자 확인
        String kakaoIdAsEmail = "kakao_" + oauthUserInfo.getOauthId();
        return userRepository.findByEmail(kakaoIdAsEmail)
            .orElseGet(() -> createKakaoUser(oauthUserInfo, kakaoIdAsEmail));
    }
    
    /**
     * 카카오 사용자 생성 (email 필드에 카카오 ID 저장)
     */
    private User createKakaoUser(com.swyp10.domain.auth.dto.common.OAuthUserInfo oauthUserInfo, String kakaoIdAsEmail) {
        // 닉네임이 없으면 기본값 설정
        String nickname = oauthUserInfo.getNickname() != null ? 
            oauthUserInfo.getNickname() : 
            "카카오사용자" + oauthUserInfo.getOauthId();
        
        // 비밀번호는 UUID로 설정 (사용하지 않지만 nullable=false이므로)
        String dummyPassword = passwordEncoder.encode(java.util.UUID.randomUUID().toString());
        
        User newUser = User.builder()
            .email(kakaoIdAsEmail) // email 필드에 카카오 ID 저장
            .password(dummyPassword) // 더미 비밀번호 (사용 안함)
            .nickname(nickname)
            .profileImage(oauthUserInfo.getProfileImage())
            .signupCompleted(true) // 카카오 로그인으로 바로 완료 처리
            .build();
        
        User savedUser = userRepository.save(newUser);
        
        log.info("카카오 사용자 생성 완료: userId={}, kakaoId={}, email={}", 
                savedUser.getUserId(), oauthUserInfo.getOauthId(), kakaoIdAsEmail);
        
        return savedUser;
    }
    
    // === 이메일 관련 ===
    
    /**
     * 이메일 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * 비밀번호 검증
     */
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
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
