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
  private Integer reply_count;
  private List<ReplyDto> replies;

  public static GetPostDetailResponseDto of(Post post, List<ReplyDto> replyDtoList) {
    String memberNickname = (post.getMember() != null) ? post.getMember().getNickname() : null;

    return GetPostDetailResponseDto.builder()
        .id(post.getId())
        .title(post.getTitle())
        .member(memberNickname)
        .nickname(post.getNickname())
        .reply_count(post.getReplyCount())
        .replies(replyDtoList)
        .build();
  }
}
