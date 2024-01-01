package com.hv.community.backend.dto.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivateEmailRequestDto {

  private String verificationCode;
  private String email;
  private String name;
  private String password;
}
