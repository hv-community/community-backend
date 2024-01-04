package com.hv.community.backend.exception;

import com.hv.community.backend.dto.ErrorResponseDto;
import io.sentry.Sentry;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MailException extends RuntimeException {

  private String id;

  public MailException(String id) {
    this.id = id;
  }

  public ErrorResponseDto handleMailException() {
    switch (this.id) {
      case "MAIL:SEND_MAIL_ERROR":
        return ErrorResponseDto.of(id, "메일 발송중 오류가 발생했습니다");
      default:
        Sentry.captureException(this);
        return ErrorResponseDto.of("unknown", "알 수 없는 오류");
    }
  }

  public String getId() {
    return id;
  }
}
