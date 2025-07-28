package com.swyp10.exception;

import com.swyp10.global.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리기
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * ApplicationException 처리
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<CommonResponse<?>> handleApplicationException(ApplicationException e) {
        log.warn("ApplicationException: {} - {}", e.getErrorCode(), e.getMessage());

        ErrorCode errorCode = e.getErrorCode();
        HttpStatus status = getHttpStatusFromErrorCode(errorCode);

        return ResponseEntity.status(status)
            .body(CommonResponse.fail(e.getMessage(), errorCode.getCode()));
    }

    /**
     * RestClientException 처리
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<CommonResponse<?>> handleRestClientException(RestClientException e) {
        log.warn("RestClientException: 네트워크 오류 '{}'", e.getMessage());
        return ResponseEntity.badRequest()
            .body(CommonResponse.fail(e.getMessage(), ErrorCode.NETWORK_ERROR.getCode()));
    }

    /**
     * MissingServletRequestParameterException 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CommonResponse<?>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("MissingServletRequestParameterException: 필수 파라미터 '{}' 누락", e.getParameterName());
        return ResponseEntity.badRequest()
            .body(CommonResponse.fail(ErrorCode.MISSING_REQUEST_PARAM.getMessage(), ErrorCode.MISSING_REQUEST_PARAM.getCode()));
    }

    /**
     * Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation Exception: {}", e.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.badRequest()
            .body(CommonResponse.fail(ErrorCode.INVALID_REQUEST_PARAM.getMessage(),
                ErrorCode.INVALID_REQUEST_PARAM.getCode(), fieldErrors));
    }

    /**
     * 일반 RuntimeException 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonResponse<?>> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                ErrorCode.INTERNAL_SERVER_ERROR.getCode()));
    }

    /**
     * 기타 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<?>> handleException(Exception e) {
        log.error("Unexpected Exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                ErrorCode.INTERNAL_SERVER_ERROR.getCode()));
    }

    /**
     * ErrorCode에 따른 HTTP Status 매핑
     */
    private HttpStatus getHttpStatusFromErrorCode(ErrorCode errorCode) {
        return switch (errorCode.getCode()) {
            case 4000, 4001, 4002, 4003, 4008, 4009, 4010, 4011, 4017, 4018, 4019 -> HttpStatus.BAD_REQUEST;
            case 4004, 4015, 4027 -> HttpStatus.NOT_FOUND;
            case 4005, 4014, 4016, 4028 -> HttpStatus.CONFLICT;
            case 4006, 4007, 4012, 4013 -> HttpStatus.UNAUTHORIZED;
            case 5000 -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}