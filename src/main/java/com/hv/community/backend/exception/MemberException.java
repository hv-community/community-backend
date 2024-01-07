package com.hv.community.backend.exception;


import com.hv.community.backend.dto.ErrorResponseDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MemberException extends RuntimeException {

  private final String id;

  public MemberException(String id) {
    this.id = id;
  }

  public ErrorResponseDto handleMemberException() {
    return switch (this.id) {
      case "MEMBER:EMAIL_FORM_ERROR" -> ErrorResponseDto.of(id, "이메일 유효성 검사를 통과하지 못했습니다");
      case "MEMBER:NICKNAME_FORM_ERROR" -> ErrorResponseDto.of(id, "닉네임 유효성 검사를 통과하지 못했습니다");
      case "MEMBER:PASSWORD_FORM_ERROR" -> ErrorResponseDto.of(id, "비밀번호 유효성 검사를 통과하지 못했습니다");
      case "MEMBER:MAIL_EXIST" -> ErrorResponseDto.of(id, "이미 존재하는 이메일입니다");
      case "MEMBER:NICKNAME_EXIST" -> ErrorResponseDto.of(id, "이미 존재하는 닉네임입니다");
      case "MEMBER:MEMBER_UNREGISTERED" -> ErrorResponseDto.of(id, "유저 정보가 없습니다");
      case "MEMBER:REFRESH_TOKEN_INVALID" -> ErrorResponseDto.of(id, "토큰이 유효하지 않습니다");
      default ->
        // 세부오류 사용자에게 넘기지않음
          ErrorResponseDto.of("MEMBER:UNKNOWN", "알 수 없는 오류");
    };
  }
}