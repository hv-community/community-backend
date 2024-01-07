package com.hv.community.backend.dto.community;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hv.community.backend.domain.community.Post;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PostDto {

  private final Long id;
  private final String title;
  private final String nickname;
  private final Date creationTime;
  private final Date modificationTime;

  public PostDto(Long id, String title, String nickname, Date creationTime,
      Date modificationTime) {
    this.id = id;
    this.title = title;
    this.nickname = nickname;
    this.creationTime = creationTime;
    this.modificationTime = modificationTime;
  }

  public static PostDto of(Post post) {
    return new PostDto(
        post.getId(),
        post.getTitle(),
        post.getMember() != null ? post.getMember().getNickname() : post.getNickname(),
        post.getCreationTime(),
        post.getModificationTime()
    );
  }
}
