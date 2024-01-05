package com.hv.community.backend.dto.community;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCreateRequestDto {

  private String title;
  private String content;
  private String nickname;
  private String password;
}
