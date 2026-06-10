package com.g5.teamtaskmanagement.security;

import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.entity.RefreshToken;
import com.g5.teamtaskmanagement.repository.RefreshTokenRepository;
import com.g5.teamtaskmanagement.repository.UserRepository;
import io.jsonwebtoken.Claims;
import com.g5.teamtaskmanagement.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);
        if (!jwtService.validateToken(token)) {
            writeUnauthorized(response, "Invalid access token");
            return;
        }
        Claims claims = jwtService.parseToken(token).getBody();
        String subject = claims.getSubject();
        Object sessionIdValue = claims.get("sid");
        Long sessionId = sessionIdValue instanceof Number ? ((Number) sessionIdValue).longValue() : null;
        if (sessionId == null) {
            writeUnauthorized(response, "Session is no longer valid. Please log in again.");
            return;
        }
        Optional<RefreshToken> sessionOpt = refreshTokenRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            writeUnauthorized(response, "Session is no longer valid. Please log in again.");
            return;
        }
        RefreshToken session = sessionOpt.get();
        if (session.getRevokedAt() != null || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            writeUnauthorized(response, "Session is no longer valid. Please log in again.");
            return;
        }
        if (subject != null) {
            try {
                Long userId = Long.parseLong(subject);
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    CustomUserDetails userDetails = new CustomUserDetails(user);
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,
                            null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Object> body = ApiResponse.failure("UNAUTHORIZED", message, List.of());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
