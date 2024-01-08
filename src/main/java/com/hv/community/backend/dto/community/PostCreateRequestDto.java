package com.hv.community.backend.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCreateRequestDto {

  @Schema(description = "Title of the post", example = "Example Title")
  private String title;
  @Schema(description = "Content of the post", example = "Example Content")
  private String content;
  @Schema(description = "Nickname of the post", example = "John Doe")
  private String nickname;
  @Schema(description = "Password of the post", example = "1q2w3e4r!")
  private String password;
}
