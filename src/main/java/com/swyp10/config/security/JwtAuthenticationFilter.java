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
        
        log.debug("=== JWT Filter Processing ====");
        log.debug("Request URI: {}", requestURI);
        log.debug("Authorization Header: {}", authHeader != null ? "Bearer ***" : "null");
        
        if (authHeader != null && authHeader.startsWith(AuthConstants.BEARER_PREFIX)) {
            try {
                String token = authHeader.replace(AuthConstants.BEARER_PREFIX, "");
                log.debug("Extracted token: {}...", token.substring(0, Math.min(token.length(), 20)));
                
                // 토큰 유효성 검증
                if (tokenService.validateToken(token)) {
                    String tokenType = tokenService.getTokenType(token);
                    log.debug("Token type: {}", tokenType);
                    
                    if (TokenType.USER.getValue().equals(tokenType)) {
                        // USER 토큰인 경우
                        Long userId = tokenService.getUserIdFromToken(token);
                        log.info("JWT 인증 성공: userId={}", userId);
                        
                        // Authentication 객체 생성 (userId를 principal로 설정)
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userId, 
                            null, 
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("SecurityContext에 인증 정보 설정 완료");
                    } else {
                        log.warn("USER 토큰이 아님: tokenType={}", tokenType);
                    }
                } else {
                    log.warn("토큰 검증 실패");
                }
            } catch (ApplicationException e) {
                log.warn("JWT 토큰 처리 실패: {}", e.getMessage());
                // 토큰이 유효하지 않으면 인증 정보를 설정하지 않고 계속 진행
            } catch (Exception e) {
                log.error("JWT 필터에서 예상치 못한 오류 발생", e);
            }
        } else {
            log.debug("Authorization 헤더 없음 또는 Bearer가 아님");
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
               path.startsWith("/actuator/") ||
               (path.startsWith("/api/v1/festivals/") && "GET".equals(request.getMethod()) && !path.contains("/reviews") && !path.contains("/bookmarks"));
    }
}
