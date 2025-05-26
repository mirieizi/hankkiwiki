package com.hankki.common.security.filter;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hankki.common.security.jwt.TokenProvider;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);
    private final TokenProvider tokenProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    // 필터를 스킵할 경로 패턴
    private static final List<String> WHITELIST_PATTERNS = List.of(
        "/auth/login",
        "/auth/signup",
        "/auth/check-email",
        "/auth/check-nickname",
        "/actuator/health"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // getRequestURI()는 context path를 포함한 전체 경로를 반환
        String path = request.getRequestURI();
        // context path를 제거 (없으면 그대로)
        String contextPath = request.getContextPath();
        String pathWithoutContext = contextPath.isEmpty() ? path : path.substring(contextPath.length());
        
        logger.debug("[TokenFilter] Checking path: {} (without context: {})", path, pathWithoutContext);
        
        // 패턴 매칭으로 확인
        boolean shouldSkip = WHITELIST_PATTERNS.stream()
            .anyMatch(pattern -> pathMatcher.match(pattern, pathWithoutContext));
            
        logger.debug("[TokenFilter] Path {} will {}be filtered", path, shouldSkip ? "NOT " : "");
        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain)
            throws ServletException, IOException {
        
        logger.debug("[TokenFilter] Processing request URI: {}", request.getRequestURI());

        // 헤더 확인 및 토큰 추출
        String header = request.getHeader(HEADER_AUTHORIZATION);
        String token = resolveToken(header);
        logger.debug("[TokenFilter] Resolved token: {}", token != null ? "exists" : "null");

        try {
            if (token != null && tokenProvider.validateAccessToken(token)) {
                Authentication auth = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.debug("[TokenFilter] JWT authentication succeeded for user: {}", auth.getName());
            } else {
                logger.debug("[TokenFilter] No JWT token found or token is invalid");
            }
        } catch (JwtException | IllegalArgumentException ex) {
            logger.error("[TokenFilter] Invalid JWT token: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
            return;
        } catch (Exception ex) {
            logger.error("[TokenFilter] Unexpected error in TokenAuthenticationFilter", ex);
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal authentication error");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(String header) {
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}