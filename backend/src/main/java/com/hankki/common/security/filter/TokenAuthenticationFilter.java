package com.hankki.common.security.filter;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hankki.common.security.jwt.TokenProvider;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);
    private final TokenProvider tokenProvider;

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX          = "Bearer ";

    // 필터를 스킵할 경로를 여기서 정의
    private static final List<String> WHITELIST = List.of(
        "/user/signup",
        "/user/login",
        "/user/refresh",
        "/actuator/health"
    );

    // whitelist 경로는 아예 필터 진입 자체를 건너뛴다
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return WHITELIST.contains(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HEADER_AUTHORIZATION);
        String token  = resolveToken(header);

        try {
            if (token != null && tokenProvider.validateAccessToken(token)) {
                Authentication auth = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.debug("JWT authentication succeeded for user: {}", auth.getName());
            } else {
                // 토큰이 없거나 유효하지 않아도, 여기선 로그만 남기고 체인 진행
                logger.debug("No JWT token found or token is invalid for request: {}", request.getServletPath());
            }
        } catch (JwtException | IllegalArgumentException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
            return;
        } catch (Exception ex) {
            logger.error("Unexpected error in TokenAuthenticationFilter", ex);
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
