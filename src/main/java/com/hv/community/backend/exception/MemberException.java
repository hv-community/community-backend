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
      case "MEMBER:EMPTY_ACCESS_TOKEN" -> ErrorResponseDto.of(id, "액세스토큰이 비어있습니다");
      case "MEMBER:SIGNUP_FAIL" -> ErrorResponseDto.of(id, "가입중 오류가 발생했습니다");
      case "MEMBER:CREATE_EMAIL_VERIFICATION_CODE_FAIL" ->
          ErrorResponseDto.of(id, "활성화코드 생성중 오류가 발생했습니다");
      case "MEMBER:GET_EMAIL_VERIFICATION_CODE_FAIL" ->
          ErrorResponseDto.of(id, "활성화코드를 가져오는중 오류가 발생했습니다");
      case "MEMBER:ACTIVATE_EMAIL_FAIL" -> ErrorResponseDto.of(id, "이메일 활성화중 오류가 발생했습니다");
      case "MEMBER:EMAIL_OR_PASSWORD_ERROR" -> ErrorResponseDto.of(id, "이메일 혹은 비밀번호가 틀렸습니다");
      case "MEMBER:EMAIL_ACTIVATE_REQUIRE" -> ErrorResponseDto.of(id, "이메일 활성화가 필요합니다");
      case "MEMBER:GET_MY_PROFILE_ERROR" -> ErrorResponseDto.of(id, "프로필을 가져오는중 오류가 발생했습니다");
      default -> ErrorResponseDto.of("MEMBER:UNKNOWN", "알 수 없는 오류");
    };
  }
}