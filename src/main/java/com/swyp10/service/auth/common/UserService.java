package com.swyp10.service.auth.common;

import com.swyp10.dto.auth.common.OAuthUserInfo;
import com.swyp10.dto.auth.common.SignupRequest;
import com.swyp10.entity.LoginType;
import com.swyp10.entity.OAuthAccount;
import com.swyp10.entity.User;
import com.swyp10.repository.OAuthAccountRepository;
import com.swyp10.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final OAuthAccountRepository oauthAccountRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 사용자 ID로 사용자 조회
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
    }
    
    /**
     * OAuth 계정 찾기 또는 생성 (User는 생성하지 않음)
     */
    @Transactional
    public OAuthAccount findOrCreateOAuthAccount(OAuthUserInfo oauthUserInfo) {
        LoginType provider = LoginType.valueOf(oauthUserInfo.getProvider().name());
        String providerUserId = oauthUserInfo.getOauthId();
        
        return oauthAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)
            .map(existingAccount -> {
                updateOAuthAccountInfo(existingAccount, oauthUserInfo);
                log.info("기존 OAuth 계정 로그인: accountId={}, provider={}", 
                        existingAccount.getOauthId(), provider);
                return existingAccount;
            })
            .orElseGet(() -> {
                OAuthAccount newAccount = createNewOAuthAccount(oauthUserInfo);
                log.info("새 OAuth 계정 생성: accountId={}, provider={}", 
                        newAccount.getOauthId(), provider);
                return newAccount;
            });
    }
    
    /**
     * 추가 회원가입 완료 (OAuth 계정과 User 연결)
     */
    @Transactional
    public User completeAdditionalSignup(Long oauthAccountId, SignupRequest request) {
        OAuthAccount oauthAccount = getOAuthAccount(oauthAccountId);
        
        // 이미 연결된 User가 있는지 확인
        if (oauthAccount.getUser() != null) {
            throw new RuntimeException("이미 회원가입이 완료된 OAuth 계정입니다");
        }
        
        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다");
        }
        
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
        oauthAccount.setUser(savedUser);
        oauthAccountRepository.save(oauthAccount);
        
        log.info("추가 회원가입 완료: userId={}, oauthAccountId={}", 
                savedUser.getUserId(), oauthAccountId);
        
        return savedUser;
    }
    
    /**
     * OAuth 계정으로 User 찾기
     */
    public User findUserByOAuthAccount(Long oauthAccountId) {
        OAuthAccount oauthAccount = getOAuthAccount(oauthAccountId);
        
        if (oauthAccount.getUser() == null) {
            throw new RuntimeException("연결된 사용자가 없습니다. 추가 회원가입이 필요합니다.");
        }
        
        return oauthAccount.getUser();
    }
    
    /**
     * OAuth 계정의 회원가입 완료 여부 확인
     */
    public boolean isSignupCompleted(Long oauthAccountId) {
        OAuthAccount oauthAccount = getOAuthAccount(oauthAccountId);
        return oauthAccount.getUser() != null && oauthAccount.getUser().getSignupCompleted();
    }
    
    /**
     * OAuth 계정 정보 조회
     */
    public OAuthAccount getOAuthAccount(Long oauthAccountId) {
        return oauthAccountRepository.findById(oauthAccountId)
            .orElseThrow(() -> new RuntimeException("OAuth 계정을 찾을 수 없습니다: " + oauthAccountId));
    }
    
    /**
     * 일반 이메일 회원가입
     */
    @Transactional
    public User createEmailUser(String email, String password, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 사용 중인 이메일입니다");
        }
        
        User newUser = User.builder()
            .email(email)
            .password(passwordEncoder.encode(password))
            .nickname(nickname)
            .loginType(LoginType.EMAIL)
            .signupCompleted(true)
            .build();
        
        return userRepository.save(newUser);
    }
    
    // === Private 메서드들 ===
    
    /**
     * 새로운 OAuth 계정 생성
     */
    private OAuthAccount createNewOAuthAccount(OAuthUserInfo oauthUserInfo) {
        LoginType provider = LoginType.valueOf(oauthUserInfo.getProvider().name());
        
        OAuthAccount newAccount = OAuthAccount.builder()
            .provider(provider)
            .providerUserId(oauthUserInfo.getOauthId())
            .providerEmail(oauthUserInfo.getEmail())
            .providerNickname(oauthUserInfo.getNickname())
            .providerProfileImage(oauthUserInfo.getProfileImage())
            .user(null) // 추가 회원가입 시 연결
            .build();
            
        return oauthAccountRepository.save(newAccount);
    }
    
    /**
     * 기존 OAuth 계정 정보 업데이트
     */
    private void updateOAuthAccountInfo(OAuthAccount oauthAccount, OAuthUserInfo oauthUserInfo) {
        boolean updated = false;
        
        if (hasChanged(oauthUserInfo.getEmail(), oauthAccount.getProviderEmail())) {
            oauthAccount.setProviderEmail(oauthUserInfo.getEmail());
            updated = true;
        }
        
        if (hasChanged(oauthUserInfo.getNickname(), oauthAccount.getProviderNickname())) {
            oauthAccount.setProviderNickname(oauthUserInfo.getNickname());
            updated = true;
        }
        
        if (hasChanged(oauthUserInfo.getProfileImage(), oauthAccount.getProviderProfileImage())) {
            oauthAccount.setProviderProfileImage(oauthUserInfo.getProfileImage());
            updated = true;
        }
        
        if (updated) {
            oauthAccountRepository.save(oauthAccount);
            log.info("OAuth 계정 정보 업데이트: accountId={}", oauthAccount.getOauthId());
        }
    }
    
    /**
     * 값 변경 여부 확인 유틸리티
     */
    private boolean hasChanged(String newValue, String oldValue) {
        return newValue != null && !newValue.equals(oldValue);
    }
}
