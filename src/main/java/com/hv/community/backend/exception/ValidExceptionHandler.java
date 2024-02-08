package com.hv.community.backend.exception;

import com.hv.community.backend.dto.ErrorResponseDto;

public class ValidExceptionHandler {

  public static ErrorResponseDto handleMethodArgumentNotValidException(String id) {
    return switch (id) {
      case "VALID:EMAIL_FORM_ERROR" -> ErrorResponseDto.build(id, "이메일 유효성 검사를 통과하지 못했습니다");
      case "VALID:NICKNAME_FORM_ERROR" -> ErrorResponseDto.build(id, "닉네임 유효성 검사를 통과하지 못했습니다");
      case "VALID:PASSWORD_FORM_ERROR" -> ErrorResponseDto.build(id, "비밀번호 유효성 검사를 통과하지 못했습니다");
      case "VALID:TOKEN_INVALID" -> ErrorResponseDto.build(id, "토큰이 유효하지 않습니다");
      case "VALID:CODE_INVALID" -> ErrorResponseDto.build(id, "코드가 유효하지 않습니다");
      default -> ErrorResponseDto.build("VALID:UNKNOWN", "알 수 없는 오류");
    };
  }
}
