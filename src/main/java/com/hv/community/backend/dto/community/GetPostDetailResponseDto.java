package com.hv.community.backend.dto.community;

import com.hv.community.backend.domain.community.Post;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GetPostDetailResponseDto {

  private Long id;
  private String title;
  private String member;
  private String nickname;
  private Long member_id;
  private String content;
  private Integer reply_count;
  private List<ReplyDto> replies;
  private int currentPage;
  private int totalPages;
  private Integer prev;
  private Integer next;

  public static GetPostDetailResponseDto of(Post post, List<ReplyDto> replyDtoList,
      int currentPage, int totalPages,
      boolean hasPrevious, boolean hasNext) {
    String memberNickname = (post.getMember() != null) ? post.getMember().getNickname() : null;
    Long memberId = (post.getMember() != null) ? post.getMember().getId() : null;

    Integer prev = (!hasPrevious) ? null : currentPage;
    Integer next = (!hasNext) ? null : currentPage + 2;
    return GetPostDetailResponseDto.builder()
        .id(post.getId())
        .title(post.getTitle())
        .member(memberNickname)
        .nickname(post.getNickname())
        .member_id(memberId)
        .content(post.getContent())
        .reply_count(post.getReplyCount())
        .replies(replyDtoList)
        .currentPage(currentPage + 1)
        .totalPages(totalPages)
        .prev(prev)
        .next(next)
        .build();
  }
}
