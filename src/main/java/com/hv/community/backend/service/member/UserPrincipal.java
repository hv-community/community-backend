package com.hv.community.backend.service.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hv.community.backend.domain.member.Member;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

  private final Long id;

  private final String email;

  @JsonIgnore
  private final String password;

  public UserPrincipal(Long id, String email, String password) {
    this.id = id;
    this.email = email;
    this.password = password;
  }

  public static UserPrincipal create(Member member) {
    return new UserPrincipal(
        member.getId(),
        member.getEmail(),
        member.getPassword()
    );
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return null;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return false;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return false;
  }

  @Override
  public boolean isEnabled() {
    return false;
  }
}
