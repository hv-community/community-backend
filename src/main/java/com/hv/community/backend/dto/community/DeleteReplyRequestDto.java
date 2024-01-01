package com.hv.community.backend.dto.community;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteReplyRequestDto {

  private Long reply_id;
  private String password;
}
