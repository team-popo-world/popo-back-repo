package com.popoworld.backend.global.token;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().equals("/auth/token/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = parseJwt(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmailFromToken(token);
            String userId = jwtTokenProvider.getUserIdFromToken(token);
            String tokenSessionId = jwtTokenProvider.getSessionIdFromToken(token);

            // Redis에 저장된 현재 유효한 세션 ID
            String redisSessionId = redisTemplate.opsForValue().get("session:" + email);

            // 세션 ID 불일치 → 다른 브라우저에서 로그인됨
            if (redisSessionId == null || !redisSessionId.equals(tokenSessionId)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"중복 로그인으로 세션이 만료되었습니다.\"}");
                return;
            }
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email, // principal (또는 User 객체)
                            null,
                            Collections.emptyList() // 권한 없음 처리
                    );

            // userId는 details에 담아둠
            authentication.setDetails(userId);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }


        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }

}
