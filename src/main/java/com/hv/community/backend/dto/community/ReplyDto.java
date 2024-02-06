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
public class ReplyDto {

  private Long id;
  private String content;
  private String nickname;
  private Member memberId;
  private Date creationTime;
  private Date modificationTime;

  @Builder
  public ReplyDto(Long id, String content, String nickname, Member memberId, Date creationTime,
      Date modificationTime) {
    this.id = id;
    this.content = content;
    this.nickname = nickname;
    this.memberId = memberId;
    this.creationTime = creationTime;
    this.modificationTime = modificationTime;
  }
}
