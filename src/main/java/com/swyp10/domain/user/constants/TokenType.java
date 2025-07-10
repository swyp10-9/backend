package com.swyp10.domain.user.constants;

/**
 * 토큰 타입 열거형
 */
public enum TokenType {
    USER("USER"),
    OAUTH("OAUTH");
    
    private final String value;
    
    TokenType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static TokenType fromString(String value) {
        for (TokenType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("알 수 없는 토큰 타입: " + value);
    }
}
