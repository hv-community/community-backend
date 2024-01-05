package com.hv.community.backend.exception;

import com.hv.community.backend.dto.ErrorResponseDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CommunityException extends RuntimeException {

  private final String id;

  public CommunityException(String id) {
    this.id = id;
  }

  public ErrorResponseDto handleCommunityException() {
    switch (this.id) {
      case "COMMUNITY:PAGE_INVALID":
        return ErrorResponseDto.of(id, "페이지가 존재하지 않습니다");
      case "COMMUNITY:COMMUNITY_INVALID":
        return ErrorResponseDto.of(id, "커뮤니티가 존재하지 않습니다");
      case "COMMUNITY:POST_INVALID":
        return ErrorResponseDto.of(id, "게시글이 존재하지 않습니다");
      case "COMMUNITY:REPLY_INVALID":
        return ErrorResponseDto.of(id, "댓글이 존재하지 않습니다");
      case "COMMUNITY:EMPTY_TITLE_OR_CONTENT":
        return ErrorResponseDto.of(id, "제목 혹은 내용이 비어있습니다");
      case "COMMUNITY:PASSWORD_INVALID":
        return ErrorResponseDto.of(id, "비밀번호가 일치하지 않습니다");
      case "COMMUNITY:PERMISSION_INVALID":
        return ErrorResponseDto.of(id, "권한이 없습니다");
      default:
        return ErrorResponseDto.of("COMMUNITY:UNKNOWN", "알 수 없는 커뮤니티 오류");
    }
  }
}