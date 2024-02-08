package com.hv.community.backend.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupRequestDto {

  @NotBlank(message = "VALID:EMAIL_FORM_ERROR")
  @Pattern(regexp = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
      + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", message = "VALID:EMAIL_FORM_ERROR")
  @Schema(description = "Example Email", example = "community@ruu.kr")
  private String email;


  @NotBlank(message = "VALID:NICKNAME_FORM_ERROR")
  @Size(min = 2, max = 20, message = "VALID:NICKNAME_FORM_ERROR")
  @Schema(description = "Example Nickname", example = "nickname")
  private String nickname;


  @NotBlank(message = "VALID:PASSWORD_FORM_ERROR")
  @Size(min = 8, max = 20, message = "VALID:PASSWORD_FORM_ERROR")
  @Pattern(regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>/?]+.*", message = "VALID:PASSWORD_FORM_ERROR")
  @Schema(description = "Example Password", example = "1q2w3e4r!")
  private String password;
}