package com.swyp10.global.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private Integer code;         // 에러 코드(성공 시 null)
    private Object errorDetail;   // 추가 에러 정보(필드에러 등, 필요시)

    // 성공 응답
    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(true, data, null, null, null);
    }
    // 실패 응답(필수)
    public static <T> CommonResponse<T> fail(String message, Integer code) {
        return new CommonResponse<>(false, null, message, code, null);
    }
    // 실패 응답(필드 에러 등 추가정보 포함)
    public static <T> CommonResponse<T> fail(String message, Integer code, Object errorDetail) {
        return new CommonResponse<>(false, null, message, code, errorDetail);
    }
}
