package com.hv.community.backend.dto.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SigninRequestDto {

  private String email;
  private String password;
}
