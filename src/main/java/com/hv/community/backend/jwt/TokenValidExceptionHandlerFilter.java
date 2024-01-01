package com.hv.community.backend.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hv.community.backend.dto.ResponseDto;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


@Component
public class TokenValidExceptionHandlerFilter extends OncePerRequestFilter {

  private final Logger logger = LoggerFactory.getLogger(TokenValidExceptionHandlerFilter.class);

  private final ObjectMapper objectMapper;

  @Autowired
  public TokenValidExceptionHandlerFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
    // 만료된 토큰
    catch (ExpiredJwtException expiredJwtException) {
      logger.debug("ExpiredJwtException 예외 발생");
      httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
      httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
      httpServletResponse.setCharacterEncoding("UTF-8");
      Map<String, String> errorCode = new HashMap<>();
      errorCode.put("accessToken", "TOKEN_EXPIRED");
      objectMapper.writeValue(httpServletResponse.getWriter(),
          ResponseDto.builder().status("401").message("TOKEN_EXPIRED").errors(errorCode).build());
    }
    // 잘못된 토큰
    catch (InvalidAccessTokenException invalidAccessTokenException) {
      logger.debug("InvalidAccessTokenException 예외 발생");
      httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
      httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
      httpServletResponse.setCharacterEncoding("UTF-8");
      Map<String, String> errorCode = new HashMap<>();
      errorCode.put("accessToken", "TOKEN_INVALID");
      objectMapper.writeValue(httpServletResponse.getWriter(),
          ResponseDto.builder().status("401").message("TOKEN_INVALID").errors(errorCode).build());
    }
  }
}
