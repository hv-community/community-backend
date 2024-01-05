package com.hv.community.backend.dto.community;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostUpdateRequestDto {

  private String title;
  private String content;
  private String password;
}
