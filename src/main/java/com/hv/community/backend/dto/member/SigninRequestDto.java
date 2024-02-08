package com.hv.community.backend.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class SigninRequestDto {


  @NotBlank(message = "VALID:EMAIL_FORM_ERROR")
  @Pattern(regexp = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
      + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", message = "VALID:EMAIL_FORM_ERROR")
  @Schema(description = "Example Email", example = "community@ruu.kr")
  private String email;

  @NotBlank(message = "VALID:PASSWORD_FORM_ERROR")
  @Schema(description = "Example Password", example = "1q2w3e4r!")
  private String password;
}
