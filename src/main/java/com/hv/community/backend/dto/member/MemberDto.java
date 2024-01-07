package com.hv.community.backend.dto.member;


import com.hv.community.backend.domain.member.Member;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDto {

  private Long id;
  private String email;
  private String nickname;

  public static MemberDto fromEntity(Member member) {
    MemberDto memberDto = new MemberDto();
    memberDto.setId(member.getId());
    memberDto.setEmail(member.getEmail());
    memberDto.setNickname(member.getNickname());
    return memberDto;
  }
}
