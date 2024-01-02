package com.hv.community.backend.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


public class JwtFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer";

  private final TokenProvider tokenProvider;

  public JwtFilter(TokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String jwt = resolveToken(request);
    String requestURI = request.getRequestURI();

    if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
      Authentication authentication = tokenProvider.getAuthentication(jwt);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      logger.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri:{} token: {}",
          authentication.getName(), requestURI, jwt);
    } else {
      logger.debug("유효한 JWT 토큰이 없거나 JWT 토큰이 비어있습니다, uri:{}", requestURI);
    }
    filterChain.doFilter(request, response);
  }


  // 요청 헤더 또는 쿠키에서 jwt 추출
  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX.length() + 1);
    }
    return null;
  }
}
