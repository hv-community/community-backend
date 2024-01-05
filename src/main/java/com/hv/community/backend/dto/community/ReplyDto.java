package com.hv.community.backend.dto.community;

import com.hv.community.backend.domain.community.Reply;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReplyDto {

  private Long id;
  private String reply;
  private String nickname;
  private Date creationTime;
  private Date modificationTime;


  public static ReplyDto of(Reply reply) {
    return ReplyDto.builder()
        .id(reply.getId())
        .reply(reply.getReply())
        .nickname(reply.getNickname())
        .creationTime(reply.getCreationTime())
        .modificationTime(reply.getModificationTime())
        .build();
  }
}
