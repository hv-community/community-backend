package com.hv.community.backend.dto.community;

import com.hv.community.backend.domain.community.Post;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@Builder
public class PostListResponseDto {

  private final int page;
  private final int pageSize;
  private final int prev;
  private final int next;
  private final List<PostDto> items;

  public PostListResponseDto(int page, int pageSize, int prev, int next, List<PostDto> items) {
    this.page = page;
    this.pageSize = pageSize;
    this.prev = prev;
    this.next = next;
    this.items = items;
  }

  public static PostListResponseDto of(Page<Post> postPage, int pageSize) {
    List<PostDto> postListResponseDtos = postPage.stream()
        .map(PostDto::of)
        .collect(Collectors.toList());

    return new PostListResponseDto(
        postPage.getNumber(),
        pageSize,
        postPage.getNumber() - 1,
        postPage.getNumber() + 1,
        postListResponseDtos
    );
  }
}
