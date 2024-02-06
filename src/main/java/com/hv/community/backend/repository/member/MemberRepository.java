package com.hv.community.backend.repository.member;


import com.hv.community.backend.domain.member.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByEmail(String email);

  Optional<Member> findByToken(String token);

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);
}
