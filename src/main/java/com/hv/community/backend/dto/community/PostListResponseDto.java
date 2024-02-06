package com.hv.community.backend.dto.community;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PostListResponseDto {

  private Integer next;
  private Integer prev;
  private int totalPage;
  private int page;
  private int pageSize;
  private List<PostDto> items;

  @Builder
  public PostListResponseDto(Integer next, Integer prev, int totalPage, int page, int pageSize,
      List<PostDto> items) {
    this.next = next;
    this.prev = prev;
    this.totalPage = totalPage;
    this.page = page;
    this.pageSize = pageSize;
    this.items = items;
  }
}
