package com.hv.community.backend.dto.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivateEmailRequestDto {

  private String token;
  private String verification_code;
}
