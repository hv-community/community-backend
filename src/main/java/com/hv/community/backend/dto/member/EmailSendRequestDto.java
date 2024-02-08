package com.hv.community.backend.dto.member;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EmailSendRequestDto {

  @NotBlank(message = "VALID:TOKEN_INVALID")
  private String token;
}
