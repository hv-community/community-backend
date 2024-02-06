package com.hv.community.backend.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
@Table(name = "role")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {

  @Id
  @Column(name = "role_name", length = 50)
  private String roleName;

  @Builder
  public Role(String roleName) {
    this.roleName = roleName;
  }

  public SimpleGrantedAuthority createGrantedAuthorities() {
    return new SimpleGrantedAuthority(this.roleName);
  }
}