package com.swyp10.domain.auth.service.common;

import com.swyp10.domain.auth.dto.OAuthProvider;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * OAuth 제공자별 클라이언트를 생성하고 관리하는 팩토리 클래스
 */
@Component
@RequiredArgsConstructor
public class OAuthClientFactory {
    
    private final Map<String, OAuthClient> oauthClients;
    
    /**
     * 제공자에 맞는 OAuth 클라이언트 반환
     * @param provider OAuth 제공자
     * @return 해당 제공자의 OAuth 클라이언트
     * @throws ApplicationException 지원하지 않는 제공자인 경우
     */
    public OAuthClient getClient(OAuthProvider provider) {
        OAuthClient client = oauthClients.get(provider.getValue() + "OAuthClient");
        
        if (client == null) {
            throw new ApplicationException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }
        
        return client;
    }
}
