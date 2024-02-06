package com.hv.community.backend.domain.community;

import com.hv.community.backend.domain.member.Member;
import com.hv.community.backend.dto.community.IdResponseDto;
import com.hv.community.backend.dto.community.ReplyDto;
import com.hv.community.backend.dto.community.ReplyUpdateRequestDto;
import com.hv.community.backend.exception.CommunityException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Calendar;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "reply")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reply {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "content")
  private String content;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "creation_time")
  private Date creationTime;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "modification_time")
  private Date modificationTime;


  @ManyToOne
  @JoinColumn(name = "member_id", referencedColumnName = "id")
  private Member member;

  @Column(name = "nickname")
  private String nickname;

  // member아닌경우 password저장
  @Column(name = "password")
  private String password;

  @ManyToOne
  @JoinColumn(name = "community_id", referencedColumnName = "id")
  private Community community;

  @ManyToOne
  @JoinColumn(name = "post_id", referencedColumnName = "id")
  private Post post;

  @Builder
  public Reply(Long id, String content, Date creationTime, Date modificationTime, Member member,
      String nickname, String password, Community community, Post post) {
    this.id = id;
    this.content = content;
    this.creationTime = creationTime;
    this.modificationTime = modificationTime;
    this.member = member;
    this.nickname = nickname;
    this.password = password;
    this.community = community;
    this.post = post;
  }

  public IdResponseDto buildIdResponseDto() {
    return IdResponseDto.builder()
        .id(this.id)
        .build();
  }

  private static final String COMMUNITY_PERMISSION_INVALID = "COMMUNITY:PERMISSION_INVALID";

  public boolean checkPassword(PasswordEncoder passwordEncoder, String password) {
    if (this.password == null) {
      throw new CommunityException(COMMUNITY_PERMISSION_INVALID);
    }
    return passwordEncoder.matches(this.password, password);
  }

  public boolean checkMember(String email) {
    return this.member.checkMember(email);
  }

  public void editReply(ReplyUpdateRequestDto replyUpdateRequestDto) {
    Calendar calendar = Calendar.getInstance();
    Date currentDate = calendar.getTime();

    this.content = replyUpdateRequestDto.getContent();
    this.modificationTime = currentDate;
  }

  public ReplyDto buildReplyDto() {
    return ReplyDto.builder()
        .id(this.id)
        .content(this.content)
        .nickname(this.nickname)
        .memberId(member)
        .creationTime(this.creationTime)
        .modificationTime(this.modificationTime)
        .build();
  }

  public void deleteReply() {
    this.post.deleteReply();
  }
}
