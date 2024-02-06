package com.hv.community.backend.domain.community;

import com.hv.community.backend.domain.member.Member;
import com.hv.community.backend.dto.community.IdResponseDto;
import com.hv.community.backend.dto.community.PostDetailResponseDto;
import com.hv.community.backend.dto.community.PostDto;
import com.hv.community.backend.dto.community.PostUpdateRequestDto;
import com.hv.community.backend.dto.community.ReplyCreateRequestDto;
import com.hv.community.backend.exception.CommunityException;
import com.hv.community.backend.repository.community.PostRepository;
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
@Table(name = "post")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "title")
  private String title;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "creation_time")
  private Date creationTime;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "modification_time")
  private Date modificationTime;

  @Column(name = "content")
  private String content;

  @Column(name = "reply_count")
  private int replyCount;

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

  @Builder
  public Post(Long id, String title, Date creationTime, Date modificationTime, String content,
      int replyCount, Member member, String nickname, String password,
      Community community) {
    this.id = id;
    this.title = title;
    this.creationTime = creationTime;
    this.modificationTime = modificationTime;
    this.content = content;
    this.replyCount = replyCount;
    this.member = member;
    this.nickname = nickname;
    this.password = password;
    this.community = community;
  }

  public PostDto buildPostDto() {
    return PostDto.builder()
        .id(this.id)
        .title(this.title)
        .replyCount(this.replyCount)
        .nickname(this.nickname)
        .creationTime(this.creationTime)
        .modificationTime(this.modificationTime)
        .build();
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

  public PostDetailResponseDto buildPostDetailResponseDto(Long previousPostId,
      Long nextPostId) {
    return PostDetailResponseDto.builder()
        .id(this.id)
        .title(this.title)
        .nickname(this.nickname)
        .memberId(this.member)
        .content(this.content)
        .replyCount(this.replyCount)
        .previousId(previousPostId)
        .nextId(nextPostId)
        .creationTime(this.creationTime)
        .modificationTime(this.modificationTime)
        .build();
  }

  public boolean checkMember(String email) {
    return this.member.checkMember(email);
  }

  public void editPost(PostUpdateRequestDto postUpdateRequestDto) {
    Calendar calendar = Calendar.getInstance();
    Date currentDate = calendar.getTime();

    this.title = postUpdateRequestDto.getTitle();
    this.content = postUpdateRequestDto.getContent();
    this.modificationTime = currentDate;
  }

  public Reply createReply(ReplyCreateRequestDto replyCreateRequestDto, String password,
      Member member) {
    this.replyCount += 1;
    return this.community.createReply(replyCreateRequestDto, password, member, this);
  }

  public Long findPreviousPostId(PostRepository postRepository) {
    return this.community.findPreviousPost(postRepository, this.id).id;
  }

  public Long findNextPostId(PostRepository postRepository) {
    return this.community.findNextPost(postRepository, this.id).id;
  }

  public void deleteReply() {
    this.replyCount -= 1;
  }
}
