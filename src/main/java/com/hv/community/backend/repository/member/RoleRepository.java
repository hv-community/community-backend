package com.hv.community.backend.repository.member;


import com.hv.community.backend.domain.member.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {

  Optional<Role> findByRoleName(String roleName);
}
