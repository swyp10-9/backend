package com.swyp10.config.security;

import com.swyp10.constants.AuthConstants;
import com.swyp10.constants.TokenType;
import com.swyp10.domain.auth.service.common.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class OptionalUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final TokenService tokenService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(OptionalUserId.class) &&
               parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String authHeader = request.getHeader("Authorization");
        
        System.out.println("=== OptionalUserIdResolver Debug ===");
        System.out.println("Authorization Header: " + (authHeader != null ? "Bearer ***" : "null"));
        
        if (authHeader != null && authHeader.startsWith(AuthConstants.BEARER_PREFIX)) {
            try {
                String token = authHeader.replace(AuthConstants.BEARER_PREFIX, "");
                System.out.println("Token extracted");
                
                // 토큰 유효성 검증
                if (tokenService.validateToken(token)) {
                    String tokenType = tokenService.getTokenType(token);
                    System.out.println("Token type: " + tokenType);
                    
                    if (TokenType.USER.getValue().equals(tokenType)) {
                        Long userId = tokenService.getUserIdFromToken(token);
                        System.out.println("Resolved userId: " + userId);
                        return userId;
                    } else {
                        System.out.println("Not USER token, returning null");
                        return null;
                    }
                } else {
                    System.out.println("Token validation failed, returning null");
                    return null;
                }
            } catch (Exception e) {
                System.out.println("Token processing error: " + e.getMessage() + ", returning null");
                return null;
            }
        } else {
            System.out.println("No Authorization header, returning null");
            return null;
        }
    }
}
