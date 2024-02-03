package com.hv.community.backend.dto.member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationCodeDto {

  private String email;
  private String verificationCode;

  @Builder
  public EmailVerificationCodeDto(String email, String verificationCode) {
    this.email = email;
    this.verificationCode = verificationCode;
  }
}
