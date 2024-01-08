package com.hv.community.backend.dto.community;

import com.hv.community.backend.domain.community.Community;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommunityDto {

  private Long id;
  private String title;
  private String description;
  private String thumbnail;

  public static CommunityDto of(Community community) {
    return CommunityDto.builder()
        .id(community.getId())
        .title(community.getTitle())
        .description(community.getDescription())
        .thumbnail(community.getThumbnail())
        .build();
  }
}
