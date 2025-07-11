package com.swyp10.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
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
    public ResponseEntity<Map<String, Object>> handleApplicationException(ApplicationException e) {
        log.warn("ApplicationException: {} - {}", e.getErrorCode(), e.getMessage());
        
        ErrorCode errorCode = e.getErrorCode();
        HttpStatus status = getHttpStatusFromErrorCode(errorCode);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("code", errorCode.getCode());
        response.put("message", errorCode.getMessage());
        
        return ResponseEntity.status(status).body(response);
    }

    /**
     * RestClientException 처리
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> RestClientException(
            RestClientException e) {
        log.warn("RestClientException: 네트워크 오류 '{}'", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("code", 5001);
        response.put("message", e.getMessage());


        return ResponseEntity.badRequest().body(response);
    }

    /**
     * MissingServletRequestParameterException 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> MissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        log.warn("MissingServletRequestParameterException: 필수 파라미터 '{}' 누락", e.getParameterName());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("code", 4020);
        response.put("message", e.getMessage());


        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation Exception: {}", e.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("code", 4022);
        response.put("message", "입력값이 올바르지 않습니다.");
        response.put("fieldErrors", fieldErrors);
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 일반 RuntimeException 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException: {}", e.getMessage(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("code", 4000);
        response.put("message", e.getMessage());
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 기타 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("Unexpected Exception: {}", e.getMessage(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("code", 5000);
        response.put("message", "서버 내부 오류가 발생했습니다.");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * ErrorCode에 따른 HTTP Status 매핑
     */
    private HttpStatus getHttpStatusFromErrorCode(ErrorCode errorCode) {
        return switch (errorCode.getCode()) {
            case 4000, 4001, 4002, 4003, 4008, 4009, 4010, 4011, 4017, 4018, 4019 -> HttpStatus.BAD_REQUEST;
            case 4004, 4015 -> HttpStatus.NOT_FOUND;
            case 4005, 4014, 4016 -> HttpStatus.CONFLICT;
            case 4006, 4007, 4012, 4013 -> HttpStatus.UNAUTHORIZED;
            case 5000 -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
