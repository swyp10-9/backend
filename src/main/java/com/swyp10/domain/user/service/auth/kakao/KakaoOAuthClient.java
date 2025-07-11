package com.swyp10.domain.user.service.auth.kakao;

import com.swyp10.domain.user.constants.AuthConstants;
import com.swyp10.domain.user.dto.auth.common.OAuthUserInfo;
import com.swyp10.domain.user.dto.auth.kakao.KakaoTokenResponse;
import com.swyp10.domain.user.dto.auth.kakao.KakaoUserResponse;
import com.swyp10.global.exception.ApplicationException;
import com.swyp10.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class KakaoOAuthClient {

    private final RestTemplate restTemplate;
    
    @Value("${oauth.kakao.client-id}")
    private String KAKAO_CLIENT_ID;
    
    @Value("${oauth.kakao.redirect-uri}")
    private String KAKAO_REDIRECT_URI;
    
    @Value("${oauth.kakao.token-url}")
    private String KAKAO_TOKEN_URL;
    
    @Value("${oauth.kakao.user-info-url}")
    private String KAKAO_USER_INFO_URL;

    public KakaoOAuthClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * 인가 코드로 Kakao Access Token 발급
     */
    public KakaoTokenResponse getAccessToken(String code) {
        HttpEntity<MultiValueMap<String, String>> request = createTokenRequest(code);
        
        log.info("카카오 토큰 발급 요청: clientId={}, redirectUri={}", 
                KAKAO_CLIENT_ID, KAKAO_REDIRECT_URI);
        
        ResponseEntity<String> response = restTemplate.exchange(
            KAKAO_TOKEN_URL,
            HttpMethod.POST,
            request,
            String.class
        );
        
        validateResponse(response);
        
        return parseTokenResponse(response.getBody());
    }

    /**
     * Kakao Access Token으로 사용자 정보 조회
     */
    public OAuthUserInfo getUserInfo(String accessToken) {
        HttpEntity<String> entity = createUserInfoRequest(accessToken);

        log.info("카카오 사용자 정보 API 호출 시작");

        ResponseEntity<KakaoUserResponse> response = restTemplate.exchange(
            KAKAO_USER_INFO_URL,
            HttpMethod.GET,
            entity,
            KakaoUserResponse.class
        );

        KakaoUserResponse kakaoUser = validateUserResponse(response);
        
        validateUserData(kakaoUser);

        log.info("카카오 사용자 정보 조회 성공: id={}, email={}, nickname={}",
                kakaoUser.getId(),
                kakaoUser.getKakaoAccount().getEmail(),
                kakaoUser.getKakaoAccount().getProfile().getNickname());

        return OAuthUserInfo.fromKakao(kakaoUser);
    }
    
    // === Private 메서드들 ===
    
    /**
     * 토큰 요청 생성
     */
    private HttpEntity<MultiValueMap<String, String>> createTokenRequest(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", AuthConstants.CONTENT_TYPE_FORM_URLENCODED);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", AuthConstants.GRANT_TYPE_AUTHORIZATION_CODE);
        params.add("client_id", KAKAO_CLIENT_ID);
        params.add("redirect_uri", KAKAO_REDIRECT_URI);
        params.add("code", code);
        
        return new HttpEntity<>(params, headers);
    }
    
    /**
     * 사용자 정보 요청 생성
     */
    private HttpEntity<String> createUserInfoRequest(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(headers);
    }
    
    /**
     * 응답 유효성 검증
     */
    private void validateResponse(ResponseEntity<String> response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ApplicationException(ErrorCode.KAKAO_TOKEN_EXCEPTION);
        }
    }
    
    /**
     * 토큰 응답 파싱
     */
    private KakaoTokenResponse parseTokenResponse(String responseBody) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(responseBody);

            String accessToken = (String) json.get("access_token");
            String refreshToken = (String) json.get("refresh_token");
            Object expires = json.get("expires_in");
            Long expiresIn = expires != null ? ((Number) expires).longValue() : null;

            log.info("카카오 액세스 토큰 발급 성공");

            return KakaoTokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(expiresIn)
                    .build();

        } catch (Exception e) {
            log.error("카카오 토큰 응답 파싱 실패", e);
            throw new ApplicationException(ErrorCode.TOKEN_PARSING_FAILED);
        }
    }

    /**
     * 사용자 응답 유효성 검증
     */
    private KakaoUserResponse validateUserResponse(ResponseEntity<KakaoUserResponse> response) {
        KakaoUserResponse kakaoUser = response.getBody();
        if (kakaoUser == null) {
            throw new ApplicationException(ErrorCode.KAKAO_USER_INFO_EXCEPTION);
        }
        return kakaoUser;
    }
    
    /**
     * 사용자 데이터 유효성 검증
     */
    private void validateUserData(KakaoUserResponse kakaoUser) {
        if (kakaoUser.getKakaoAccount() == null) {
            throw new ApplicationException(ErrorCode.KAKAO_USER_INFO_EXCEPTION);
        }

        if (kakaoUser.getKakaoAccount().getProfile() == null) {
            throw new ApplicationException(ErrorCode.KAKAO_USER_INFO_EXCEPTION);
        }

        if (kakaoUser.getKakaoAccount().getEmail() == null) {
            log.warn("카카오 이메일 정보가 없습니다. 사용자가 이메일 동의를 거부했습니다.");
        }
    }
}
