package com.hv.community.backend.dto.community;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCommunityRequestDto {

  private String title;
  private String description;
}
