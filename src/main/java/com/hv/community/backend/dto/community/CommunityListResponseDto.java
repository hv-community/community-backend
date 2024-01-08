package com.hv.community.backend.dto.community;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hv.community.backend.domain.community.Community;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CommunityListResponseDto {

  private Integer next;
  private Integer prev;
  private int totalPage;
  private int page;
  private int pageSize;
  private List<CommunityDto> items;

  public static CommunityListResponseDto of(List<CommunityDto> communityDtoList,
      Page<Community> communityPage,
      int pageSize) {
    int currentPage = communityPage.getNumber() + 1;

    Integer prev = (!communityPage.hasPrevious()) ? null : currentPage - 1;
    Integer next = (!communityPage.hasNext()) ? null : currentPage + 1;

    return CommunityListResponseDto.builder()
        .next(next)
        .prev(prev)
        .totalPage(communityPage.getTotalPages())
        .page(currentPage)
        .pageSize(pageSize)
        .items(communityDtoList)
        .build();
  }
}
