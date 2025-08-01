package com.swyp10.domain.auth.dto.common;

import com.swyp10.domain.auth.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 사용자 정보 응답 DTO
 */
@Getter
@Schema(description = "사용자 정보")
public class UserInfo {
    @Schema(description = "사용자 ID", required = true, nullable = false, example = "12345")
    private final Long userId;

    @Schema(description = "이메일 주소", required = true, nullable = false, example = "user@example.com")
    private final String email;

    @Schema(description = "닉네임", required = true, nullable = false, example = "홍길동")
    private final String nickname;

    private UserInfo(Long userId, String email, String nickname) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
    }

    public static UserInfo from(User user) {
        return new UserInfo(user.getUserId(), user.getEmail(), user.getNickname());
    }

}