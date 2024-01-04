package com.hv.community.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponseDto {

  private final String id;
  private final String message;

  public ErrorResponseDto(String id, String message) {
    this.id = id;
    this.message = message;
  }

  public static ErrorResponseDto of(String id, String message) {
    return ErrorResponseDto.builder().id(id).message(message).build();

  }
}
