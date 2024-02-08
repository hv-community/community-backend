package com.hv.community.backend.domain.community;

import com.hv.community.backend.domain.member.Member;
import com.hv.community.backend.dto.community.CommunityDto;
import com.hv.community.backend.dto.community.PostCreateRequestDto;
import com.hv.community.backend.dto.community.ReplyCreateRequestDto;
import com.hv.community.backend.repository.community.PostRepository;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Calendar;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Community {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "community")
  private String title;

  @Column(name = "description")
  private String description;

  @Column(name = "thumbnail")
  private String thumbnail;

  @Builder
  public Community(Long id, String title, String description, String thumbnail) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.thumbnail = thumbnail;
  }

  public Post createPost(PostCreateRequestDto postCreateRequestDto, String password,
      Member member) {
    // 유저일때만 email저장 아니면 code만 저장
    if (member == null) {
//      // 이 부분은 @Validated 로 구현
//      if (postCreateRequestDto.getNickname().length() < 2) {
//        throw new CommunityException("COMMUNITY:UNAVAILABLE_USER_NAME");
//      }
      Calendar calendar = Calendar.getInstance();
      Date currentDate = calendar.getTime();

      return Post.builder()
          .title(postCreateRequestDto.getTitle())
          .creationTime(currentDate)
          .modificationTime(currentDate)
          .content(postCreateRequestDto.getContent())
          .replyCount(0)
          .nickname(postCreateRequestDto.getNickname())
          .password(password)
          .community(this)
          .build();
    }
    return member.createPost(postCreateRequestDto, this);
  }

  public CommunityDto buildCommunityDto() {
    return CommunityDto.builder()
        .id(this.id)
        .title(this.title)
        .description(this.description)
        .thumbnail(this.thumbnail)
        .build();
  }

  public Reply createReply(ReplyCreateRequestDto replyCreateRequestDto, String password,
      Member member, Post post) {
    if (member == null) {
      Calendar calendar = Calendar.getInstance();
      Date currentDate = calendar.getTime();

      return Reply.builder()
          .content(replyCreateRequestDto.getContent())
          .creationTime(currentDate)
          .modificationTime(currentDate)
          .nickname(replyCreateRequestDto.getNickname())
          .password(password)
          .community(this)
          .post(post)
          .build();
    }
    return member.createReply(replyCreateRequestDto, post, this);
  }

  public Post findPreviousPost(PostRepository postRepository, Long postId) {
    return postRepository.findTopByCommunityIdAndIdLessThanOrderByIdDesc(
        this.id, postId).orElse(null);
  }

  public Post findNextPost(PostRepository postRepository, Long postId) {
    return postRepository.findTopByCommunityIdAndIdGreaterThanOrderByIdAsc(
        this.id, postId).orElse(null);
  }
}
