package com.hv.community.backend.exception;

import com.hv.community.backend.dto.ErrorResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommunityException extends RuntimeException {

  private final String id;

  public ErrorResponseDto handleCommunityException() {
    return switch (this.id) {
      case "COMMUNITY:PAGE_INVALID" -> ErrorResponseDto.build(id, "페이지가 존재하지 않습니다");
      case "COMMUNITY:COMMUNITY_INVALID" -> ErrorResponseDto.build(id, "커뮤니티가 존재하지 않습니다");
      case "COMMUNITY:POST_INVALID" -> ErrorResponseDto.build(id, "게시글이 존재하지 않습니다");
      case "COMMUNITY:REPLY_INVALID" -> ErrorResponseDto.build(id, "댓글이 존재하지 않습니다");
      case "COMMUNITY:PASSWORD_INVALID" -> ErrorResponseDto.build(id, "비밀번호가 일치하지 않습니다");
      case "COMMUNITY:PERMISSION_INVALID" -> ErrorResponseDto.build(id, "권한이 없습니다");
      default -> ErrorResponseDto.build("COMMUNITY:UNKNOWN", "알 수 없는 커뮤니티 오류");
    };
  }
}