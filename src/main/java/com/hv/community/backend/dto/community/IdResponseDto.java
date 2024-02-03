package com.hv.community.backend.dto.community;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdResponseDto {

  private Long id;

  @Builder
  public IdResponseDto(Long id) {
    this.id = id;
  }
}
