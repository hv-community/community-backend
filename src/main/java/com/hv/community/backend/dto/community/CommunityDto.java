package com.hv.community.backend.dto.community;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityDto {

  private Long id;
  private String title;
  private String description;
  private String thumbnail;

  @Builder
  public CommunityDto(Long id, String title, String description, String thumbnail) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.thumbnail = thumbnail;
  }
}
