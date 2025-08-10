package com.swyp10.config.security;

import com.swyp10.constants.AuthConstants;
import com.swyp10.constants.TokenType;
import com.swyp10.domain.auth.service.common.TokenService;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");
        
        // 로그 출력
        System.out.println("[JWT FILTER] URI: " + requestURI + ", Auth: " + (authHeader != null ? "Bearer ***" : "null"));
        
        if (authHeader != null && authHeader.startsWith(AuthConstants.BEARER_PREFIX)) {
            try {
                String token = authHeader.replace(AuthConstants.BEARER_PREFIX, "");
                
                // 토큰 유효성 검증
                if (tokenService.validateToken(token)) {
                    String tokenType = tokenService.getTokenType(token);
                    
                    if (TokenType.USER.getValue().equals(tokenType)) {
                        // USER 토큰인 경우
                        Long userId = tokenService.getUserIdFromToken(token);
                        System.out.println("[JWT FILTER] userId: " + userId);
                        
                        // Authentication 객체 생성 (userId를 principal로 설정)
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userId, 
                            null, 
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("[JWT FILTER] Authentication set");
                    }
                }
            } catch (Exception e) {
                System.out.println("[JWT FILTER] Error: " + e.getMessage());
                // 에러가 나도 계속 진행
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // 인증이 필요없는 경로들
        return path.startsWith("/api/v1/auth/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/favicon.ico") ||
               path.startsWith("/actuator/");
        // GET 요청 필터링 제거 - 모든 API 요청에서 JWT 필터 실행
    }
}
