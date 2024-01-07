package com.hv.community.backend.exception;

import com.hv.community.backend.dto.ErrorResponseDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MailException extends RuntimeException {

  private final String id;

  public MailException(String id) {
    this.id = id;
  }

  public ErrorResponseDto handleMailException() {
    if (this.id.equals("MAIL:SEND_MAIL_FAIL")) {
      return ErrorResponseDto.of(id, "메일 발송중 오류가 발생했습니다");
    }
    return ErrorResponseDto.of("UNKNOWN", "알 수 없는 오류");
  }
}
