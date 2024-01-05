package com.hv.community.backend.jwt;


import com.hv.community.backend.dto.TokenDto;
import com.hv.community.backend.exception.MemberException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


@Component
public class TokenProvider implements InitializingBean {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private static final String AUTHORITIES_KEY = "auth";
  private final String secret;
  private final long ACCESS_TOKEN_EXPIRE_TIME;
  private final long REFRESH_TOKEN_EXPIRE_TIME;
  private Key key;

  public TokenProvider(@Value("${jwt.secret}") String secret,
      @Value("${jwt.access-token-expire-time}") long accessTokenExpireTime,
      @Value("${jwt.refresh-token-expire-time}") long refreshTokenExpireTime) {
    this.secret = secret;
    this.ACCESS_TOKEN_EXPIRE_TIME = accessTokenExpireTime;
    this.REFRESH_TOKEN_EXPIRE_TIME = refreshTokenExpireTime;
  }

  @Override
  public void afterPropertiesSet() {
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  // Authentication 의 권한 정보를 이용해 토큰 생성
  public TokenDto createToken(Authentication authentication) {
    // 로그인 시도 유저의 권한들
    String authorities = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));
    long now = (new Date()).getTime();
    Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
    // Create Access Token
    String accessToken = Jwts.builder()
        .signWith(key, SignatureAlgorithm.HS512)        // header  "alg": "HS512"
        .setSubject(authentication.getName())           // payload "sub": "email@gmail.com"
        .claim(AUTHORITIES_KEY, authorities)            // payload "auth": "ROLE_USER"
        .setExpiration(accessTokenExpiresIn)            // payload "exp": 1516240000
        .compact();

    // Create Refresh Token
    Date refreshTokenExpiresIn = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);
    String refreshToken = Jwts.builder()
        .signWith(key, SignatureAlgorithm.HS512)
        .setSubject(authentication.getName())
        .claim(AUTHORITIES_KEY, authorities)
        .setExpiration(refreshTokenExpiresIn)
        .compact();

    TokenDto tokenDto = new TokenDto();
    tokenDto.setAccessToken(accessToken);
    tokenDto.setRefreshToken(refreshToken);
    return tokenDto;
  }

  public String refreshAccessToken(String refreshToken) {
    Claims refreshClaims = parseClaims(refreshToken);
    Authentication authentication = getAuthentication(refreshToken);
    String newAccessToken = createToken(authentication).getAccessToken();
    return newAccessToken;
  }

  // accessToken 정보 반환
  public Authentication getAuthentication(String accessToken) {
    // 토큰 복호화
    Claims claims = parseClaims(accessToken);
    logger.debug(String.valueOf(claims));
    if (claims.get(AUTHORITIES_KEY) == null) {
      throw new MemberException("MEMBER:GET_AUTHENTICATION_FAIL");
    }

    // claims 에서 권한 목록 획득
    Collection<? extends GrantedAuthority> authorities = Arrays.stream(
            claims.get(AUTHORITIES_KEY).toString().split(","))
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
    // UserDetails 첨부한 Authentication(인증 정보) 반환
    UserDetails principal = new User(claims.getSubject(), "", authorities);
    return new UsernamePasswordAuthenticationToken(principal, accessToken, authorities);
  }

  private Claims parseClaims(String accessToken) {
    try {
      return Jwts.parserBuilder().setSigningKey(key).build()
          .parseClaimsJws(accessToken)
          .getBody();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }

  // JwtFilter-doFilterInternal에서 OncePerRequestFilter를 통해 요청들어올때 마다 검사
  // header에 jwt가 있을때 jwt값 검증
  // 에러를 TokenValidExceptionHandlerFilter로 넘겨줌
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
      logger.info("잘못된 JWT 서명입니다.");
      throw new InvalidAccessTokenException();
    } catch (ExpiredJwtException expiredJwtException) {
      logger.info("만료된 JWT 토큰입니다.");
      throw expiredJwtException;
    } catch (UnsupportedJwtException e) {
      logger.info("미지원 JWT 토큰입니다.");
      throw new InvalidAccessTokenException();
    } catch (IllegalArgumentException e) {
      logger.info("잘못된 JWT 토큰입니다.");
      throw new InvalidAccessTokenException();
    }
  }
}
