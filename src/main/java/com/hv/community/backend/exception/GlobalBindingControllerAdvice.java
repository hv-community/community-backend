package com.hv.community.backend.exception;

import com.hv.community.backend.dto.ErrorResponseDto;
import io.sentry.Sentry;
import io.sentry.protocol.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class GlobalBindingControllerAdvice {

  private void configureSentryScope(@AuthenticationPrincipal String id,
      HttpServletRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Sentry.withScope(scope -> {
      // IP 항상 기본으로 수집
      User user = new User();
      user.setIpAddress(request.getRemoteAddr());
      // 로그인 상태라면 email 추가 수집
      if (authentication.getName() != null) {
        user.setEmail(authentication.getName());
      }
      Sentry.captureMessage(id);
    });
  }

  // Parameter @Validated
  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponseDto handleValidatedException(ConstraintViolationException e,
      HttpServletRequest request) {
    String errorMessage = e.getConstraintViolations().stream()
        .findFirst()
        .map(ConstraintViolation::getMessage)
        .orElse("Validation failed");

    configureSentryScope(errorMessage, request);
    log.debug("{}", errorMessage);
    return ValidatedExceptionHandler.handleConstraintViolationException(errorMessage);
  }

  // @RequestBody @Valid
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponseDto handleValidException(MethodArgumentNotValidException e,
      HttpServletRequest request) {
    String errorMessage = e.getBindingResult().getAllErrors().stream()
        .findFirst()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .orElse("Validation failed");

    configureSentryScope(errorMessage, request);
    log.debug("{}", errorMessage);
    return ValidExceptionHandler.handleMethodArgumentNotValidException(errorMessage);
  }

  @ExceptionHandler(MemberException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponseDto handleMemberException(MemberException e, HttpServletRequest request) {
    configureSentryScope(e.getId(), request);
    log.debug("{}", e.getId());
    return e.handleMemberException();
  }

  @ExceptionHandler(CommunityException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponseDto handleCommunityException(CommunityException e,
      HttpServletRequest request) {
    configureSentryScope(e.getId(), request);
    log.debug("{}", e.getId());
    return e.handleCommunityException();
  }

  @ExceptionHandler(MailException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponseDto handleMailException(MailException e, HttpServletRequest request) {
    configureSentryScope(e.getId(), request);
    log.debug("MAIL_ERROR : {}", e.getId());
    return e.handleMailException();
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  public ErrorResponseDto handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
      HttpServletRequest request) {
    configureSentryScope("METHOD:METHOD_NOT_ALLOWED_" + ex.getMethod(), request);
    log.debug("METHOD_ERROR : {}", ex.getMethod());
    return ErrorResponseDto.build("METHOD:METHOD_NOT_ALLOWED_" + ex.getMethod(), "허용되지않은 요청방법입니다");
  }
}