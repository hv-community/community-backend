package com.hv.community.backend.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hv.community.backend.dto.ErrorResponseDto;
import io.jsonwebtoken.ExpiredJwtException;
import io.sentry.Sentry;
import io.sentry.protocol.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class TokenValidExceptionHandlerFilter extends OncePerRequestFilter {

  private final ObjectMapper objectMapper;

  @Autowired
  public TokenValidExceptionHandlerFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  // JwtFilter-doFilterInternal > TokenProvider-validateToken에서 오류발생시 실행
  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      filterChain.doFilter(request, response);
    }
    // 만료된 토큰
    catch (ExpiredJwtException expiredJwtException) {
      log.debug("ExpiredJwtException 예외 발생");
      tokenException("TOKEN:TOKEN_EXPIRED", "만료된 JWT 토큰입니다", request, response);
    }
    // 잘못된 토큰
    catch (InvalidAccessTokenException invalidAccessTokenException) {
      log.debug("InvalidAccessTokenException 예외 발생");
      tokenException("TOKEN:TOKEN_INVALID", "잘못된 JWT 토큰입니다", request, response);
    }
  }

  private void tokenException(String id, String message, HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(),
        ErrorResponseDto.of(id, message));
    configureSentryScope(id, request);
  }

  private void configureSentryScope(String id, HttpServletRequest request) {
    Sentry.withScope(scope -> {
      // IP 항상 기본으로 수집
      User user = new User();
      user.setIpAddress(request.getRemoteAddr());
      Sentry.setUser(user);
      Sentry.captureMessage(id);
    });
  }
}
