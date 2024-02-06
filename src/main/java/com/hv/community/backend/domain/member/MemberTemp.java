package com.hv.community.backend.domain.member;

import com.hv.community.backend.dto.member.EmailVerificationCodeDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_temp")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTemp {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "token")
  private String token;

  @Column(name = "code")
  private String code;

  @OneToOne
  @JoinColumn(name = "member_id", referencedColumnName = "id")
  private Member member;

  @Builder
  public MemberTemp(String token, String code, Member member) {
    this.token = token;
    this.code = code;
    this.member = member;
  }

  // verificationCode 와 memberTemp의 code 확인
  public boolean checkVerificationCode(String verificationCode) {
    return Objects.equals(verificationCode, this.code);
  }

  public EmailVerificationCodeDto buildEmailVerificationCodeDto(String email) {
    return EmailVerificationCodeDto.builder()
        .email(email)
        .verificationCode(this.code)
        .build();
  }
}