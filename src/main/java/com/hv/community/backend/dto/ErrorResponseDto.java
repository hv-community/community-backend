package com.hv.community.backend.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponseDto {

  private String id;
  private String message;

  @Builder
  public ErrorResponseDto(String id, String message) {
    this.id = id;
    this.message = message;
  }

  public static ErrorResponseDto of(String id, String message) {
    return ErrorResponseDto.builder().id(id).message(message).build();
  }
}
