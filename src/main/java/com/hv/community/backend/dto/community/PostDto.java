package com.hv.community.backend.dto.community;

import com.hv.community.backend.domain.community.Post;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostDto {

  private final Long id;
  private final String title;
  private final String nickname;
  private final Date creation_time;
  private final Date modification_time;

  public PostDto(Long id, String title, String nickname, Date creationTime,
      Date modificationTime) {
    this.id = id;
    this.title = title;
    this.nickname = nickname;
    this.creation_time = creationTime;
    modification_time = modificationTime;
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
