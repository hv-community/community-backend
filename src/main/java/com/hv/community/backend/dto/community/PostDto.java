package com.hv.community.backend.dto.community;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostDto {

  private Long id;
  private String title;
  private int replyCount;
  private String nickname;
  private Date creationTime;
  private Date modificationTime;

  @Builder
  public PostDto(Long id, String title, int replyCount, String nickname, Date creationTime,
      Date modificationTime) {
    this.id = id;
    this.title = title;
    this.replyCount = replyCount;
    this.nickname = nickname;
    this.creationTime = creationTime;
    this.modificationTime = modificationTime;
  }
}
