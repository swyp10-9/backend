package com.swyp10.config.security;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthenticationPrincipalArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class) &&
               parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        System.out.println("=== ArgumentResolver Debug ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null ? authentication.isAuthenticated() : "null"));
        System.out.println("Principal: " + (authentication != null ? authentication.getPrincipal() : "null"));
        
        // 인증된 사용자가 있으면 userId 반환, 없으면 null 반환 (예외 없이)
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            try {
                Long userId = (Long) authentication.getPrincipal();
                System.out.println("Resolved userId: " + userId);
                return userId;
            } catch (ClassCastException e) {
                System.out.println("Principal is not Long type: " + authentication.getPrincipal().getClass());
                return null;
            }
        }
        
        System.out.println("No valid authentication found, returning null");
        return null; // 기본적으로 null 반환 (선택적 인증)
    }
}
