package com.hv.community.backend.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hv.community.backend.dto.ErrorResponseDto;
import io.sentry.Sentry;
import io.sentry.protocol.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

  private final Logger logger = LoggerFactory.getLogger(JwtAccessDeniedHandler.class);

  private final ObjectMapper objectMapper;

  @Autowired
  public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException {
    logger.debug("JwtAccessDeniedHandler 발생");
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(),
        ErrorResponseDto.of("ACCESS:FORBIDDEN_ACCESS", "허가되지 않은 접근입니다"));
    configureSentryScope("ACCESS:FORBIDDEN_ACCESS", request);
  }

  private void configureSentryScope(String id, HttpServletRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Sentry.withScope(scope -> {
      // IP 항상 기본으로 수집
      User user = new User();
      user.setIpAddress(request.getRemoteAddr());
      Sentry.setUser(user);
      // 로그인 상태라면 email 추가 수집
      if (authentication.getName() != null) {
        user.setEmail(authentication.getName());
      }
      Sentry.captureMessage(id);
    });
  }
}
