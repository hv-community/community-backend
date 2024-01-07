package com.hv.community.backend.dto.community;

import com.hv.community.backend.domain.community.Post;
import com.hv.community.backend.domain.community.Reply;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@Builder
public class PostDetailResponseDto {

  private Long id;
  private String title;
  private String nickname;
  private Long memberId;
  private String content;
  private Long previousId;
  private Long nextId;
  private List<ReplyDto> replies;
  private int currentPage;
  private int totalPages;
  private Integer prev;
  private Integer next;

  public static PostDetailResponseDto of(Post post, Long previousPostId, Long nextPostId,
      List<ReplyDto> replyDtoList, Page<Reply> replyPage) {
    Long memberId = (post.getMember() != null) ? post.getMember().getId() : null;

    Integer prev = (!replyPage.hasPrevious()) ? null : replyPage.getNumber();
    Integer next = (!replyPage.hasNext()) ? null : replyPage.getNumber() + 2;
    return PostDetailResponseDto.builder()
        .id(post.getId())
        .title(post.getTitle())
        .nickname(post.getNickname())
        .memberId(memberId)
        .content(post.getContent())
        .previousId(previousPostId)
        .nextId(nextPostId)
        .replies(replyDtoList)
        .currentPage(replyPage.getNumber() + 1)
        .totalPages(replyPage.getTotalPages())
        .prev(prev)
        .next(next)
        .build();
  }
}
