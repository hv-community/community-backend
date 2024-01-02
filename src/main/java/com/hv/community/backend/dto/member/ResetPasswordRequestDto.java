package com.hv.community.backend.dto.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequestDto {

  private String verification_code;
  private String email;
  private String password;
}
