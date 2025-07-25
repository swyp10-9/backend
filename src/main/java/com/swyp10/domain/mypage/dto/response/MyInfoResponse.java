package com.swyp10.domain.mypage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyInfoResponse {
    @Schema(description = "회원 ID", example = "100")
    private Long userId;

    @Schema(description = "닉네임", example = "길훈이")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://...")
    private String profileImage;
}
