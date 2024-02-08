package com.hv.community.backend.exception;


import com.hv.community.backend.dto.ErrorResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberException extends RuntimeException {

  private final String id;

  public ErrorResponseDto handleMemberException() {
    return switch (this.id) {
      case "MEMBER:MAIL_EXIST" -> ErrorResponseDto.build(id, "이미 존재하는 이메일입니다");
      case "MEMBER:NICKNAME_EXIST" -> ErrorResponseDto.build(id, "이미 존재하는 닉네임입니다");
      case "MEMBER:MEMBER_UNREGISTERED" -> ErrorResponseDto.build(id, "유저 정보가 없습니다");
      case "MEMBER:REFRESH_TOKEN_INVALID" -> ErrorResponseDto.build(id, "토큰이 유효하지 않습니다");
      case "MEMBER:ACCESS_TOKEN_INVALID" -> ErrorResponseDto.build(id, "토큰이 유효하지 않습니다");
      case "MEMBER:EMAIL_OR_PASSWORD_ERROR" -> ErrorResponseDto.build(id, "이메일 혹은 비밀번호가 틀렸습니다");
      case "MEMBER:EMAIL_ACTIVATE_REQUIRE" -> ErrorResponseDto.build(id, "이메일 활성화가 필요합니다");
      default -> ErrorResponseDto.build("MEMBER:UNKNOWN", "알 수 없는 오류");
    };
  }
}