package com.swyp10.domain.auth.service.common;

import com.swyp10.domain.auth.dto.common.OAuthUserInfo;
import com.swyp10.domain.auth.entity.LoginType;
import com.swyp10.domain.auth.entity.OAuthAccount;
import com.swyp10.domain.auth.entity.User;
import com.swyp10.domain.auth.repository.OAuthAccountRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth 계정 관리 전용 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    
    private final OAuthAccountRepository oauthAccountRepository;
    private final UserService userService;
    /**
     * OAuth 계정 찾기 또는 생성
     */
    @Transactional
    public OAuthAccount findOrCreate(OAuthUserInfo oauthUserInfo) {
        LoginType provider = LoginType.valueOf(oauthUserInfo.getProvider().name());
        String providerUserId = oauthUserInfo.getOauthId();
        
        return oauthAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)
            .map(existingAccount -> updateAccountInfo(existingAccount, oauthUserInfo))
            .orElseGet(() -> createNewAccount(oauthUserInfo));
    }
    
    /**
     * OAuth 계정 ID로 조회
     */
    @Transactional(readOnly = true)
    public OAuthAccount findById(Long oauthAccountId) {
        return oauthAccountRepository.findById(oauthAccountId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.OAUTH_ACCOUNT_NOT_FOUND));
    }
    
    /**
     * OAuth 계정과 기존 사용자 연동
     */
    @Transactional
    public void linkOAuthAccountToUser(Long oauthAccountId, Long userId) {
        OAuthAccount oauthAccount = findById(oauthAccountId);
        
        if (oauthAccount.getUser() != null) {
            throw new ApplicationException(ErrorCode.SIGNUP_ALREADY_COMPLETED);
        }

        User user = userService.findById(userId);
        oauthAccount.setUser(user);
        
        oauthAccountRepository.save(oauthAccount);
        
        log.info("OAuth 계정과 기존 사용자 연동 완료: oauthAccountId={}, userId={}", 
                oauthAccountId, userId);
    }
    
    /**
     * OAuth 계정과 사용자 연결
     */
    @Transactional
    public void linkWithUser(Long oauthAccountId, User user) {
        OAuthAccount oauthAccount = findById(oauthAccountId);
        
        if (oauthAccount.getUser() != null) {
            throw new ApplicationException(ErrorCode.SIGNUP_ALREADY_COMPLETED);
        }
        
        oauthAccount.setUser(user);
        oauthAccountRepository.save(oauthAccount);
        
        log.info("OAuth 계정과 사용자 연결 완료: oauthAccountId={}, userId={}", 
                oauthAccountId, user.getUserId());
    }
    
    /**
     * 회원가입 완료 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isSignupCompleted(Long oauthAccountId) {
        OAuthAccount oauthAccount = findById(oauthAccountId);
        return oauthAccount.getUser() != null && oauthAccount.getUser().getSignupCompleted();
    }
    
    /**
     * OAuth 계정이 이미 다른 사용자와 연동되어 있는지 확인
     */
    @Transactional(readOnly = true)
    public boolean isOAuthAccountLinked(OAuthUserInfo oauthUserInfo) {
        LoginType provider = LoginType.valueOf(oauthUserInfo.getProvider().name());
        String providerUserId = oauthUserInfo.getOauthId();
        
        return oauthAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)
            .map(account -> account.getUser() != null)
            .orElse(false);
    }
    
    /**
     * OAuth 계정으로 사용자 찾기
     */
    @Transactional(readOnly = true)
    public User findUserByOAuthAccount(Long oauthAccountId) {
        OAuthAccount oauthAccount = findById(oauthAccountId);
        
        if (oauthAccount.getUser() == null) {
            throw new ApplicationException(ErrorCode.ADDITIONAL_SIGNUP_REQUIRED);
        }
        
        return oauthAccount.getUser();
    }
    
    // === Private 메서드들 ===
    
    /**
     * 새로운 OAuth 계정 생성
     */
    private OAuthAccount createNewAccount(OAuthUserInfo oauthUserInfo) {
        LoginType provider = LoginType.valueOf(oauthUserInfo.getProvider().name());
        
        OAuthAccount newAccount = OAuthAccount.builder()
            .provider(provider)
            .providerUserId(oauthUserInfo.getOauthId())
            .providerEmail(oauthUserInfo.getEmail())
            .providerNickname(oauthUserInfo.getNickname())
            .providerProfileImage(oauthUserInfo.getProfileImage())
            .user(null)
            .build();
            
        OAuthAccount savedAccount = oauthAccountRepository.save(newAccount);
        
        log.info("새 OAuth 계정 생성: accountId={}, provider={}", 
                savedAccount.getOauthId(), provider);
        
        return savedAccount;
    }
    
    /**
     * 기존 OAuth 계정 정보 업데이트
     */
    private OAuthAccount updateAccountInfo(OAuthAccount oauthAccount, OAuthUserInfo oauthUserInfo) {
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
        } else {
            log.info("기존 OAuth 계정 로그인: accountId={}, provider={}", 
                    oauthAccount.getOauthId(), oauthAccount.getProvider());
        }
        
        return oauthAccount;
    }
    
    /**
     * 값 변경 여부 확인
     */
    private boolean hasChanged(String newValue, String oldValue) {
        return newValue != null && !newValue.equals(oldValue);
    }
}
