package com.hv.community.backend.dto.community;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hv.community.backend.domain.member.Member;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PostDetailResponseDto {

  private Long id;
  private String title;
  private String nickname;
  private Member memberId;
  private String content;
  private int replyCount;
  private Long previousId;
  private Long nextId;
  private Date creationTime;
  private Date modificationTime;

  @Builder
  public PostDetailResponseDto(Long id, String title, String nickname, Member memberId,
      String content,
      int replyCount, Long previousId, Long nextId, Date creationTime, Date modificationTime) {
    this.id = id;
    this.title = title;
    this.nickname = nickname;
    this.memberId = memberId;
    this.content = content;
    this.replyCount = replyCount;
    this.previousId = previousId;
    this.nextId = nextId;
    this.creationTime = creationTime;
    this.modificationTime = modificationTime;
  }
}
