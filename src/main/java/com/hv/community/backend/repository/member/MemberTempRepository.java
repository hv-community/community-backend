package com.hv.community.backend.repository.member;


import com.hv.community.backend.domain.member.MemberTemp;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTempRepository extends JpaRepository<MemberTemp, Long> {

  Optional<MemberTemp> findByCode(String code);

  boolean existsByCode(String code);
}
