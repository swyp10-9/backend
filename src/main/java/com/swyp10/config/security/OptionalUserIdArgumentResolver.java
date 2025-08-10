package com.swyp10.config.security;

import com.swyp10.constants.AuthConstants;
import com.swyp10.constants.TokenType;
import com.swyp10.domain.auth.service.common.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        
        System.out.println("=== OptionalUserIdResolver ===");
        
        // 1. 먼저 SecurityContext 확인 (JWT 필터가 설정한 경우)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            try {
                Long userId = (Long) authentication.getPrincipal();
                System.out.println("SecurityContext에서 userId: " + userId);
                return userId;
            } catch (ClassCastException e) {
                System.out.println("SecurityContext principal이 Long 타입이 아님");
            }
        }
        
        // 2. SecurityContext에 없으면 HTTP 헤더에서 추출 (GET 요청 등)
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String authHeader = request.getHeader("Authorization");
        
        System.out.println("Auth Header: " + (authHeader != null ? "Bearer ***" : "null"));
        
        if (authHeader != null && authHeader.startsWith(AuthConstants.BEARER_PREFIX)) {
            try {
                String token = authHeader.replace(AuthConstants.BEARER_PREFIX, "");
                
                if (tokenService.validateToken(token)) {
                    String tokenType = tokenService.getTokenType(token);
                    
                    if (TokenType.USER.getValue().equals(tokenType)) {
                        Long userId = tokenService.getUserIdFromToken(token);
                        System.out.println("헤더에서 userId: " + userId);
                        return userId;
                    }
                }
            } catch (Exception e) {
                System.out.println("Token error: " + e.getMessage());
            }
        }
        
        System.out.println("userId 없음, null 반환");
        return null;
    }
}
