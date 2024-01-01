package com.hv.community.backend.dto.community;

import com.hv.community.backend.domain.community.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GetPostListResponseDto {

  private Long id;
  private String title;
  private Integer reply_count;
  private String member;
  private String nickname;

  public static GetPostListResponseDto of(Post post) {
    String memberNickname = (post.getMember() != null) ? post.getMember().getNickname() : null;
    String postNickname = (post.getNickname() != null) ? post.getNickname() : null;

    return GetPostListResponseDto.builder()
        .id(post.getId())
        .title(post.getTitle())
        .reply_count(post.getReplyCount())
        .member(memberNickname)
        .nickname(postNickname)
        .build();
  }
}
