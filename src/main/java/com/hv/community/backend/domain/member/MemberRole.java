package com.hv.community.backend.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
@Table(name = "member_role")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRole {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "member_id", referencedColumnName = "id")
  private Member member;

  @ManyToOne
  @JoinColumn(name = "role_name", referencedColumnName = "role_name")
  private Role role;

  @Builder
  public MemberRole(Member member, Role role) {
    this.member = member;
    this.role = role;
  }

  public SimpleGrantedAuthority createGrantedAuthorities() {
    return this.role.createGrantedAuthorities();
  }
}
