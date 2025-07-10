package com.swyp10.global.exception;

import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    public ApplicationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public ApplicationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ApplicationException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
