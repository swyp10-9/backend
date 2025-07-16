package com.swyp10.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // OAuth 관련 에러
    KAKAO_TOKEN_EXCEPTION(4001, "카카오 토큰 발급에 실패했습니다."),
    KAKAO_USER_INFO_EXCEPTION(4002, "카카오 사용자 정보 조회에 실패했습니다."),
    OAUTH_PROVIDER_NOT_SUPPORTED(4003, "지원하지 않는 OAuth 제공자입니다."),
    
    // 사용자 관련 에러
    USER_NOT_FOUND(4004, "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(4005, "이미 존재하는 사용자입니다."),
    
    // JWT 관련 에러
    INVALID_TOKEN(4006, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(4007, "만료된 토큰입니다."),
    
    // 인증 관련 추가 에러
    INVALID_OAUTH_PROVIDER(4008, "지원하지 않는 OAuth 제공자입니다."),
    OAUTH_LOGIN_FAILED(4009, "OAuth 로그인에 실패했습니다."),
    OAUTH_TOKEN_REQUIRED(4010, "OAuth 토큰이 필요합니다."),
    SIGNUP_FAILED(4011, "회원가입에 실패했습니다."),
    ADDITIONAL_SIGNUP_REQUIRED(4012, "추가 회원가입이 필요합니다."),
    UNKNOWN_TOKEN_TYPE(4013, "알 수 없는 토큰 타입입니다."),
    
    // 사용자 및 계정 관련 에러
    EMAIL_ALREADY_EXISTS(4014, "이미 사용 중인 이메일입니다."),
    OAUTH_ACCOUNT_NOT_FOUND(4015, "OAuth 계정을 찾을 수 없습니다."),
    SIGNUP_ALREADY_COMPLETED(4016, "이미 회원가입이 완료된 계정입니다."),
    INVALID_PASSWORD(4017, "비밀번호가 일치하지 않습니다."),
    
    // 토큰 관련 에러
    TOKEN_GENERATION_FAILED(4018, "토큰 생성에 실패했습니다."),
    TOKEN_PARSING_FAILED(4019, "토큰 파싱에 실패했습니다."),

    // 요청 관련 에러
    MISSING_REQUEST_PARAM(4020, "요청 파라미터가 누락되었습니다."),
    MISSING_REQUEST_HEADER(4021, "요청 헤더가 누락되었습니다."),
    INVALID_REQUEST_PARAM(4022, "요청 파라미터가 올바르지 않습니다."),
    INVALID_REQUEST_HEADER(4023, "요청 헤더가 올바르지 않습니다."),

    // 일반적인 에러
    INTERNAL_SERVER_ERROR(5000, "서버 내부 오류가 발생했습니다."),
    BAD_REQUEST(4000, "잘못된 요청입니다.");
    
    private final int code;
    private final String message;
}
