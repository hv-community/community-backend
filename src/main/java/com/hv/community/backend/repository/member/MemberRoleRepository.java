package com.hv.community.backend.repository.member;

import com.hv.community.backend.domain.member.Member;
import com.hv.community.backend.domain.member.MemberRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRoleRepository extends JpaRepository<MemberRole, Long> {

  void deleteByMember(Member member);

  List<MemberRole> findByMember(Member member);
}
