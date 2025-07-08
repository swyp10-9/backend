package com.swyp10.service.auth.kakao;

import com.swyp10.dto.auth.kakao.KakaoTokenResponse;
import com.swyp10.dto.auth.kakao.KakaoUserResponse;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KakaoOAuthClient 테스트")
class KakaoOAuthClientTest {
    @Value("${oauth.kakao.token-url}")
    private String KAKAO_TOKEN_URL;
    @Value("${oauth.kakao.user-info-url}")
    private String KAKAO_USER_INFO_URL;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private KakaoOAuthClient kakaoOAuthClient;

    @Test
    @DisplayName("카카오 액세스 토큰 발급 - 성공")
    void getAccessToken_Success() {
        // given
        String code = "test-code";
        String responseBody = "{\n" +
                "    \"access_token\": \"test-access-token\",\n" +
                "    \"token_type\": \"bearer\",\n" +
                "    \"refresh_token\": \"test-refresh-token\",\n" +
                "    \"expires_in\": 21599,\n" +
                "    \"scope\": \"profile_nickname profile_image account_email\"\n" +
                "}";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(
                eq(KAKAO_TOKEN_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))).thenReturn(responseEntity);

        // when
        String result = kakaoOAuthClient.getAccessToken(code);

        // then
        assertThat(result).isEqualTo("test-access-token");
    }

    @Test
    @DisplayName("카카오 액세스 토큰 발급 - 실패")
    void getAccessToken_Failure() {
        // given
        String code = "invalid-code";
        when(restTemplate.exchange(
                eq(KAKAO_TOKEN_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))).thenThrow(new RestClientException("토큰 발급 실패"));

        // when & then
        assertThatThrownBy(() -> kakaoOAuthClient.getAccessToken(code))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.KAKAO_TOKEN_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("카카오 사용자 정보 조회 - 성공")
    void getUserInfo_Success() {
        // given
        String accessToken = "test-access-token";

        // KakaoUserResponse 생성 - 내부 클래스는 별도 생성 후 리플렉션으로 설정
        KakaoUserResponse kakaoUserResponse = KakaoUserResponse.builder()
                .id(123456789L)
                .build();

        // 내부 클래스 생성
        KakaoUserResponse.KakaoAccount kakaoAccount = new KakaoUserResponse.KakaoAccount();
        KakaoUserResponse.KakaoAccount.Profile profile = new KakaoUserResponse.KakaoAccount.Profile();

        // 리플렉션으로 값 설정
        ReflectionTestUtils.setField(profile, "nickname", "테스트사용자");
        ReflectionTestUtils.setField(profile, "profileImageUrl", "http://example.com/profile.jpg");
        ReflectionTestUtils.setField(kakaoAccount, "email", "test@example.com");
        ReflectionTestUtils.setField(kakaoAccount, "profile", profile);
        ReflectionTestUtils.setField(kakaoUserResponse, "kakaoAccount", kakaoAccount);

        ResponseEntity<KakaoUserResponse> responseEntity = new ResponseEntity<>(kakaoUserResponse, HttpStatus.OK);
        when(restTemplate.exchange(
                eq(KAKAO_USER_INFO_URL),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KakaoUserResponse.class))).thenReturn(responseEntity);

        // when
        var result = kakaoOAuthClient.getUserInfo(accessToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getNickname()).isEqualTo("테스트사용자");
        assertThat(result.getOauthId()).isEqualTo("123456789");
        assertThat(result.getProvider().name()).isEqualTo("KAKAO");
    }

    @Test
    @DisplayName("카카오 사용자 정보 조회 - 빈 응답으로 예외 발생")
    void getUserInfo_EmptyResponse_ThrowsException() {
        // given
        String accessToken = "test-access-token";
        ResponseEntity<KakaoUserResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(
                eq(KAKAO_USER_INFO_URL),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KakaoUserResponse.class))).thenReturn(responseEntity);

        // when & then
        assertThatThrownBy(() -> kakaoOAuthClient.getUserInfo(accessToken))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.KAKAO_USER_INFO_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("카카오 사용자 정보 조회 - 카카오 계정 정보 없음")
    void getUserInfo_NoKakaoAccount_ThrowsException() {
        // given
        String accessToken = "test-access-token";
        KakaoUserResponse invalidResponse = KakaoUserResponse.builder()
                .id(123456789L)
                .build(); // kakaoAccount가 null

        ResponseEntity<KakaoUserResponse> responseEntity = new ResponseEntity<>(invalidResponse, HttpStatus.OK);
        when(restTemplate.exchange(
                eq(KAKAO_USER_INFO_URL),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KakaoUserResponse.class))).thenReturn(responseEntity);

        // when & then
        assertThatThrownBy(() -> kakaoOAuthClient.getUserInfo(accessToken))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.KAKAO_USER_INFO_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("카카오 사용자 정보 조회 - 프로필 정보 없음")
    void getUserInfo_NoProfile_ThrowsException() {
        // given
        String accessToken = "test-access-token";
        KakaoUserResponse kakaoUserResponse = KakaoUserResponse.builder()
                .id(123456789L)
                .build();

        KakaoUserResponse.KakaoAccount kakaoAccount = new KakaoUserResponse.KakaoAccount();
        ReflectionTestUtils.setField(kakaoAccount, "email", "test@example.com");
        // profile은 null로 남겨둠
        ReflectionTestUtils.setField(kakaoUserResponse, "kakaoAccount", kakaoAccount);

        ResponseEntity<KakaoUserResponse> responseEntity = new ResponseEntity<>(kakaoUserResponse, HttpStatus.OK);
        when(restTemplate.exchange(
                eq(KAKAO_USER_INFO_URL),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KakaoUserResponse.class))).thenReturn(responseEntity);

        // when & then
        assertThatThrownBy(() -> kakaoOAuthClient.getUserInfo(accessToken))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.KAKAO_USER_INFO_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("카카오 사용자 정보 조회 - 네트워크 오류")
    void getUserInfo_NetworkError_ThrowsException() {
        // given
        String accessToken = "test-access-token";
        when(restTemplate.exchange(
                eq(KAKAO_USER_INFO_URL),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KakaoUserResponse.class))).thenThrow(new RestClientException("네트워크 오류"));

        // when & then
        assertThatThrownBy(() -> kakaoOAuthClient.getUserInfo(accessToken))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.NETWORK_ERROR.getMessage());
    }
}
