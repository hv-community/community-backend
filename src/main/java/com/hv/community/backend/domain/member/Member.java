package com.hv.community.backend.domain.member;

import com.hv.community.backend.domain.community.Community;
import com.hv.community.backend.domain.community.Post;
import com.hv.community.backend.domain.community.Reply;
import com.hv.community.backend.dto.community.PostCreateRequestDto;
import com.hv.community.backend.dto.community.ReplyCreateRequestDto;
import com.hv.community.backend.dto.member.EmailVerificationCodeDto;
import com.hv.community.backend.dto.member.ProfileResponseDto;
import com.hv.community.backend.exception.MemberException;
import com.hv.community.backend.repository.member.MemberRepository;
import com.hv.community.backend.repository.member.MemberRoleRepository;
import com.hv.community.backend.repository.member.MemberTempRepository;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "nickname", unique = true)
  private String nickname;

  @Column(name = "email", unique = true)
  private String email;

  @Column(name = "password")
  private String password;

  @Column(name = "register_date")
  private Date registerDate;

  @Column(name = "emailActivated")
  private Integer emailActivated;

  @Column(name = "token")
  private String token;

  @OneToOne(mappedBy = "member")
  private MemberTemp memberTemp;

  @OneToOne(mappedBy = "member")
  private ResetVerificationCode resetVerificationCode;

  @Builder
  public Member(String nickname, String email, String password, Date registerDate,
      Integer emailActivated, String token) {
    this.nickname = nickname;
    this.email = email;
    this.password = password;
    this.registerDate = registerDate;
    this.emailActivated = emailActivated;
    this.token = token;
  }

  // 토큰값이 비어있는지 확인
  public boolean checkTokenNull() {
    return token == null;
  }

  // 토큰값이 생성된지 24시간이 지났는지 확인
  public boolean checkTokenExpired() {
    Date storedTime = registerDate;
    Date currentTime = new Date();
    long timeDifferenceMillis = currentTime.getTime() - storedTime.getTime();
    long twentyFourHoursMillis = (long) 24 * 60 * 60 * 1000;
    return timeDifferenceMillis > twentyFourHoursMillis;
  }

  // 이메일 활성화여부 확인
  public boolean checkEmailActivated() {
    return this.emailActivated == 1;
  }

  // 이메일 활성화 완료시 emailActivated -> 1, token -> null
  public void emailActivate(String verificationCode, MemberRepository memberRepository,
      MemberTempRepository memberTempRepository) {
    if (this.memberTemp != null && this.memberTemp.checkVerificationCode(verificationCode)) {
      this.emailActivated = 1;
      this.token = null;
      memberRepository.save(this);
      memberTempRepository.delete(this.memberTemp);
    } else {
      throw new MemberException("MEMBER:ACTIVATE_EMAIL_FAIL");
    }
  }

  // 인증되지않은 기존 가입자 삭제
  public void cleanUpUser(MemberTempRepository memberTempRepository,
      MemberRoleRepository memberRoleRepository, MemberRepository memberRepository) {
    memberTempRepository.delete(memberTemp);
    memberRoleRepository.deleteByMember(this);
    memberRepository.delete(this);
  }

  public EmailVerificationCodeDto buildEmailVerificationCodeDto() {
    if (this.memberTemp != null) {
      return this.memberTemp.buildEmailVerificationCodeDto(this.email);
    } else {
      // 이미 인증완료된 유저 에러
      throw new MemberException("MEMBER:MEMBER_ALREADY_ACTIVATE");
    }
  }

  public ProfileResponseDto buildProfileResponseDto() {
    return ProfileResponseDto.builder()
        .id(this.id)
        .email(this.email)
        .nickname(this.nickname)
        .build();
  }

  public Post createPost(PostCreateRequestDto postCreateRequestDto, Community community) {
    Calendar calendar = Calendar.getInstance();
    Date currentDate = calendar.getTime();

    return Post.builder()
        .title(postCreateRequestDto.getTitle())
        .creationTime(currentDate)
        .modificationTime(currentDate)
        .content(postCreateRequestDto.getContent())
        .member(this)
        .nickname(this.nickname)
        .community(community)
        .build();
  }

  public boolean checkMember(String email) {
    return Objects.equals(this.email, email);
  }

  public Reply createReply(ReplyCreateRequestDto replyCreateRequestDto, Post post,
      Community community) {
    Calendar calendar = Calendar.getInstance();
    Date currentDate = calendar.getTime();

    return Reply.builder()
        .content(replyCreateRequestDto.getContent())
        .creationTime(currentDate)
        .modificationTime(currentDate)
        .member(this)
        .nickname(this.nickname)
        .community(community)
        .post(post)
        .build();
  }

  public User buildUser(List<SimpleGrantedAuthority> grantedAuthorities) {
    return new User(this.email, this.password, grantedAuthorities);
  }
}