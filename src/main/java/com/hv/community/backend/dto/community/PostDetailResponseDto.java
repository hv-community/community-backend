package com.hv.community.backend.dto.community;

import com.hv.community.backend.domain.community.Post;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostDetailResponseDto {

  private Long id;
  private String title;
  private String nickname;
  private Long member_id;
  private String content;
  private List<ReplyDto> replies;
  private int currentPage;
  private int totalPages;
  private Integer prev;
  private Integer next;

  public static PostDetailResponseDto of(Post post, List<ReplyDto> replyDtoList,
      int currentPage, int totalPages,
      boolean hasPrevious, boolean hasNext) {
    Long memberId = (post.getMember() != null) ? post.getMember().getId() : null;

    Integer prev = (!hasPrevious) ? null : currentPage;
    Integer next = (!hasNext) ? null : currentPage + 2;
    return PostDetailResponseDto.builder()
        .id(post.getId())
        .title(post.getTitle())
        .nickname(post.getNickname())
        .member_id(memberId)
        .content(post.getContent())
        .replies(replyDtoList)
        .currentPage(currentPage + 1)
        .totalPages(totalPages)
        .prev(prev)
        .next(next)
        .build();
  }
}
