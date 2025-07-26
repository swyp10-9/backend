package com.swyp10.domain.mypage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class MyInfoUpdateRequest {
    @Schema(description = "닉네임", example = "홍길동", maxLength = 20)
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 20, message = "닉네임은 20자 이내로 입력해야 합니다.")
    private String nickname;
}
