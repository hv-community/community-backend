package com.hv.community.backend.domain.community;

import com.hv.community.backend.domain.member.Member;
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
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "post")
@Getter
@Setter
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

  // name으로 저장
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
}
