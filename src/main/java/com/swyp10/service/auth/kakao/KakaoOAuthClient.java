package com.swyp10.service.auth.kakao;

import com.swyp10.dto.auth.kakao.KakaoTokenResponse;
import com.swyp10.dto.auth.kakao.KakaoUserResponse;
import com.swyp10.dto.auth.common.OAuthUserInfo;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
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
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", KAKAO_CLIENT_ID);
            params.add("redirect_uri", KAKAO_REDIRECT_URI);
            params.add("code", code);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            
            log.info("카카오 토큰 발급 요청: URL={}, clientId={}, redirectUri={}", 
                    KAKAO_TOKEN_URL, KAKAO_CLIENT_ID, KAKAO_REDIRECT_URI);
            
            ResponseEntity<String> response = restTemplate.exchange(
                KAKAO_TOKEN_URL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ApplicationException(ErrorCode.KAKAO_TOKEN_EXCEPTION);
            }
            
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(response.getBody());
            
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
            log.error("카카오 액세스 토큰 발급 실패", e);
            throw new RuntimeException("카카오 토큰 발급에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * Kakao Access Token으로 사용자 정보 조회
     */
    public OAuthUserInfo getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("카카오 사용자 정보 API 호출 시작: URL={}", KAKAO_USER_INFO_URL);

            ResponseEntity<String> rawResponse = restTemplate.exchange(
                KAKAO_USER_INFO_URL,
                HttpMethod.GET,
                entity,
                String.class
            );

            log.info("카카오 API 응답 상태: {}", rawResponse.getStatusCode());
            log.info("카카오 API 응답 본문: {}", rawResponse.getBody());

            ResponseEntity<KakaoUserResponse> response = restTemplate.exchange(
                KAKAO_USER_INFO_URL,
                HttpMethod.GET,
                entity,
                KakaoUserResponse.class
            );

            KakaoUserResponse kakaoUser = response.getBody();
            if (kakaoUser == null) {
                throw new RuntimeException("카카오 사용자 정보를 가져올 수 없습니다.");
            }

            log.info("카카오 사용자 정보 파싱 결과: id={}, kakaoAccount={}",
                    kakaoUser.getId(), kakaoUser.getKakaoAccount());

            if (kakaoUser.getKakaoAccount() == null) {
                throw new RuntimeException("카카오 계정 정보가 없습니다. 동의항목을 확인해주세요.");
            }

            if (kakaoUser.getKakaoAccount().getProfile() == null) {
                throw new RuntimeException("카카오 프로필 정보가 없습니다. 동의항목을 확인해주세요.");
            }

            if (kakaoUser.getKakaoAccount().getEmail() == null) {
                log.warn("카카오 이메일 정보가 없습니다. 사용자가 이메일 동의를 거부했습니다.");
            }

            log.info("카카오 사용자 정보 조회 성공: id={}, email={}, nickname={}",
                    kakaoUser.getId(),
                    kakaoUser.getKakaoAccount().getEmail(),
                    kakaoUser.getKakaoAccount().getProfile().getNickname());

            return OAuthUserInfo.fromKakao(kakaoUser);

        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 실패", e);
            throw new RuntimeException("카카오 OAuth 인증에 실패했습니다: " + e.getMessage(), e);
        }
    }
}
