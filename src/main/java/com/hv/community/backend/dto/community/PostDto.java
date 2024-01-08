package com.hv.community.backend.dto.community;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hv.community.backend.domain.community.Post;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PostDto {

  private final Long id;
  private final String title;
  private final int replyCount;
  private final String nickname;
  private final Date creationTime;
  private final Date modificationTime;

  public PostDto(Long id, String title, String nickname, int replyCount, Date creationTime,
      Date modificationTime) {
    this.id = id;
    this.title = title;
    this.replyCount = replyCount;
    this.nickname = nickname;
    this.creationTime = creationTime;
    this.modificationTime = modificationTime;
  }

  public static PostDto of(Post post, int replyCount) {
    return new PostDto(post.getId(), post.getTitle(), post.getNickname(), replyCount,
        post.getCreationTime(), post.getModificationTime());
//    return PostDto.builder()
//        .id(post.getId())
//        .title(post.getTitle())
//        .replyCount(replyCount)
//        .nickname(post.getMember() != null ? post.getMember().getNickname() : post.getNickname())
//        .creationTime(post.getCreationTime())
//        .modificationTime(post.getModificationTime())
//        .build();
  }
}
