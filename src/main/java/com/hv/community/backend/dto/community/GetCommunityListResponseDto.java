package com.hv.community.backend.dto.community;

import com.hv.community.backend.domain.community.Community;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GetCommunityListResponseDto {

  private Long id;
  private String title;
  private String description;
  private String thumbnail;

  public static GetCommunityListResponseDto of(Community community) {
    return GetCommunityListResponseDto.builder()
        .id(community.getId())
        .title(community.getTitle())
        .description(community.getDescription())
        .thumbnail(community.getThumbnail())
        .build();
  }
}
