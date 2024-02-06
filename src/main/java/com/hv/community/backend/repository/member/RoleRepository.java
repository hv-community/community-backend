package com.hv.community.backend.repository.member;


import com.hv.community.backend.domain.member.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {

}
