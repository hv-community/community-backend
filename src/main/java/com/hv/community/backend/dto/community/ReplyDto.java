package com.hv.community.backend.dto.community;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hv.community.backend.domain.community.Reply;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReplyDto {

  private Long id;
  private String content;
  private String nickname;
  private Long memberId;
  private Date creationTime;
  private Date modificationTime;


  public static ReplyDto of(Reply reply) {

    return ReplyDto.builder()
        .id(reply.getId())
        .content(reply.getContent())
        .nickname(reply.getNickname())
        .memberId(reply.getMember() != null ? reply.getMember().getId() : null)
        .creationTime(reply.getCreationTime())
        .modificationTime(reply.getModificationTime())
        .build();
  }
}
