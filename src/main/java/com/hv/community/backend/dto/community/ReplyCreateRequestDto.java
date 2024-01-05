package com.hv.community.backend.dto.community;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplyCreateRequestDto {

  private String reply;
  private String nickname;
  private String password;
}
