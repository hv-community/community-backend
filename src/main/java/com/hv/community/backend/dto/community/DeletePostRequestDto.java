package com.hv.community.backend.dto.community;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeletePostRequestDto {

  private Long post_id;
  private String password;
}
