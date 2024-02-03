package com.hv.community.backend.dto.member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileResponseDto {

  private String id;
  private String email;
  private String nickname;

  @Builder
  public ProfileResponseDto(String id, String email, String nickname) {
    this.id = id;
    this.email = email;
    this.nickname = nickname;
  }
}
