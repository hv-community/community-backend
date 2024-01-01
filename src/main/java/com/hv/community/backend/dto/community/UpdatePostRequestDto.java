package com.hv.community.backend.dto.community;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePostRequestDto {

  private Long post_id;
  private String title;
  private String content;
  private String password;
}
