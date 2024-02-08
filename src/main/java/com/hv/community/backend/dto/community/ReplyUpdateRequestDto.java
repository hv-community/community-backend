package com.hv.community.backend.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ReplyUpdateRequestDto {

  @NotBlank(message = "VALID:EMPTY_CONTENT")
  @Schema(description = "Content of the post", example = "Example Content")
  private String content;

  @NotBlank(message = "VALID:PASSWORD_FORM_ERROR")
  @Size(min = 2, max = 20, message = "VALID:PASSWORD_FORM_ERROR")
  @Schema(description = "Password of the post", example = "1q2w3e4r!")
  private String password;
}
