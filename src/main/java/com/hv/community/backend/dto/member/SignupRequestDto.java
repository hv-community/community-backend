package com.hv.community.backend.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class SignupRequestDto {

  @Schema(description = "Example Email", example = "community@ruu.kr")
  private String email;
  @Schema(description = "Example Nickname", example = "nickname")
  private String nickname;
  @Schema(description = "Example Password", example = "1q2w3e4r!")
  private String password;
}
