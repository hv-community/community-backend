package com.hv.community.backend.dto.community;

import com.hv.community.backend.domain.community.Reply;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReplyDto {

  private Long id;
  private String reply;
  private String member;
  private String nickname;


  public static ReplyDto of(Reply reply) {
    String memberNickname = (reply.getMember() != null) ? reply.getMember().getNickname() : null;
    String replyNickname = (reply.getNickname() != null) ? reply.getNickname() : null;

    return ReplyDto.builder()
        .id(reply.getId())
        .reply(reply.getReply())
        .member(memberNickname)
        .nickname(replyNickname)
        .build();
  }
}
