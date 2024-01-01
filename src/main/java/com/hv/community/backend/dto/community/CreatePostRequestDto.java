package com.hv.community.backend.dto.community;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePostRequestDto {

  private Long community_id;
  private String title;
  private String content;
  private String nickname;
  private String password;
}
