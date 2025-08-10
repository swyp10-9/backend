package com.swyp10.config.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JWT 토큰에서 userId를 선택적으로 추출하는 어노테이션
 * - 토큰이 있으면 userId 반환
 * - 토큰이 없거나 유효하지 않으면 null 반환 (에러 없음)
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionalUserId {
}
