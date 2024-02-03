package com.hv.community.backend.dto;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenDto {

  private String token;

  @Builder
  public TokenDto(String token) {
    this.token = token;
  }
}
