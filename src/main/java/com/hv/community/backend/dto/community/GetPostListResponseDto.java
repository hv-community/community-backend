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
public class GetPostListResponseDto {

  private final int page;
  private final int pageSize;
  private final int prev;
  private final int next;
  private final List<PostDto> posts;

  public GetPostListResponseDto(int page, int pageSize, int prev, int next, List<PostDto> posts) {
    this.page = page;
    this.pageSize = pageSize;
    this.prev = prev;
    this.next = next;
    this.posts = posts;
  }

  public static GetPostListResponseDto of(Page<Post> postPage, int pageSize) {
    List<PostDto> postListResponseDtos = postPage.stream()
        .map(PostDto::of)
        .collect(Collectors.toList());

    return new GetPostListResponseDto(
        postPage.getNumber(),
        pageSize,
        postPage.getNumber() - 1,
        postPage.getNumber() + 1,
        postListResponseDtos
    );
  }
}
