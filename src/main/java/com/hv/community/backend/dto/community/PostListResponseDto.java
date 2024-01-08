package com.hv.community.backend.dto.community;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hv.community.backend.domain.community.Post;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PostListResponseDto {

  private Integer next;
  private Integer prev;
  private int totalPage;
  private int page;
  private int pageSize;
  private List<PostDto> items;

  public static PostListResponseDto of(List<PostDto> postDtoList,
      Page<Post> postPage, int pageSize) {
    int currentPage = postPage.getNumber() + 1;

    Integer prev = (!postPage.hasPrevious()) ? null : currentPage - 1;
    Integer next = (!postPage.hasNext()) ? null : currentPage + 1;

    return PostListResponseDto.builder()
        .next(next)
        .prev(prev)
        .totalPage(postPage.getTotalPages())
        .page(currentPage)
        .pageSize(pageSize)
        .items(postDtoList)
        .build();
  }
}
