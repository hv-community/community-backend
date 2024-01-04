package com.hv.community.backend.exception;


import com.hv.community.backend.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MemberException extends RuntimeException {

  private String id;

  public MemberException(String id) {
    this.id = id;
  }

  public ErrorResponseDto handleMemberException() {
    switch (this.id) {
      case "MEMBER:EMAIL_EMPTY":
        return ErrorResponseDto.of(id, "이메일 값이 비어있습니다");
      case "MEMBER:EMAIL_FORM_ERROR":
        return ErrorResponseDto.of(id, "이메일 유효성 검사를 통과하지 못했습니다");
      case "MEMBER:NICKNAME_FORM_ERROR":
        return ErrorResponseDto.of(id, "닉네임 유효성 검사를 통과하지 못했습니다");
      case "MEMBER:PASSWORD_FORM_ERROR":
        return ErrorResponseDto.of(id, "비밀번호 유효성 검사를 통과하지 못했습니다");
      case "MEMBER:EXIST_MAIL":
        return ErrorResponseDto.of(id, "이미 존재하는 이메일입니다");
      case "MEMBER:EXIST_NICKNAME":
        return ErrorResponseDto.of(id, "이미 존재하는 닉네임입니다");
      case "":
        return ErrorResponseDto.of(id, "");

      default:
        return ErrorResponseDto.of("unknown", "알 수 없는 오류");
    }
  }

  public String getId() {
    return id;
  }
}
