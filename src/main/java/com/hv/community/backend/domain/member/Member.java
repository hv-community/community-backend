package com.hv.community.backend.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Date;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "member")
@Getter
@Setter
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

  @OneToOne(mappedBy = "member")
  private MemberTemp memberTemp;

  @OneToOne(mappedBy = "member")
  private ResetVerificationCode resetVerificationCode;

  // jwt
  @ManyToMany
  @JoinTable(
      name = "member_role",
      joinColumns = {@JoinColumn(name = "member_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "role_name", referencedColumnName = "role_name")})
  private Set<Role> roles;
}