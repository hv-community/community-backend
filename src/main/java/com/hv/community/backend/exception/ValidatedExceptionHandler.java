package com.hv.community.backend.exception;

import com.hv.community.backend.dto.ErrorResponseDto;

public class ValidatedExceptionHandler {

  public static ErrorResponseDto handleConstraintViolationException(String id) {
    return switch (id) {
      case "VALID:PAGE_INVALID" -> ErrorResponseDto.build(id, "페이지가 존재하지 않습니다");

      case "VALID:COMMUNITY_INVALID" -> ErrorResponseDto.build(id, "게시판이 존재하지 않습니다");
      case "VALID:POST_INVALID" -> ErrorResponseDto.build(id, "게시글이 존재하지 않습니다");
      case "VALID:REPLY_INVALID" -> ErrorResponseDto.build(id, "댓글이 존재하지 않습니다");
      case "VALID:PASSWORD_INVALID" -> ErrorResponseDto.build(id, "비밀번호가 일치하지 않습니다");
      default -> ErrorResponseDto.build("VALID:UNKNOWN", "알 수 없는 오류");
    };
  }
}
