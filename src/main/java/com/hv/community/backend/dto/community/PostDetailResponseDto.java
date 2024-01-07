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
public class PostDetailResponseDto {

  private Long id;
  private String title;
  private String nickname;
  private Long memberId;
  private String content;
  private Long previousId;
  private Long nextId;
  private final Date creationTime;
  private final Date modificationTime;

  public static PostDetailResponseDto of(Post post, Long previousPostId, Long nextPostId) {
    Long memberId = (post.getMember() != null) ? post.getMember().getId() : null;

    return PostDetailResponseDto.builder()
        .id(post.getId())
        .title(post.getTitle())
        .nickname(post.getNickname())
        .memberId(memberId)
        .content(post.getContent())
        .previousId(previousPostId)
        .nextId(nextPostId)
        .creationTime(post.getCreationTime())
        .modificationTime(post.getModificationTime())
        .build();
  }
}
