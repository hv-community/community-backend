package com.hv.community.backend.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SigninRequestDto {

  @Schema(description = "Example Email", example = "community@ruu.kr")
  private String email;
  @Schema(description = "Example Password", example = "1q2w3e4r!")
  private String password;
}
