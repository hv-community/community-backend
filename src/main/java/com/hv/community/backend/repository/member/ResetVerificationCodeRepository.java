package com.hv.community.backend.repository.member;

import com.hv.community.backend.domain.member.ResetVerificationCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResetVerificationCodeRepository extends
    JpaRepository<ResetVerificationCode, Long> {

  Optional<ResetVerificationCode> findByCode(String code);

  boolean existsByCode(String code);
}
