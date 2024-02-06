package com.hv.community.backend.service.member;


import com.hv.community.backend.domain.member.Member;
import com.hv.community.backend.domain.member.MemberRole;
import com.hv.community.backend.repository.member.MemberRepository;
import com.hv.community.backend.repository.member.MemberRoleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;
  private final MemberRoleRepository memberRoleRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return memberRepository.findByEmail(email)
        .map(this::createUserDetails)
        .orElseThrow(() -> new UsernameNotFoundException(email + " -> 데이터베이스에서 찾을 수 없습니다."));
  }

  // DB 의 Member 값으로 UserDetails 객체 생성 후 반환
  private UserDetails createUserDetails(Member member) {

    log.info(member.toString());

    List<MemberRole> memberRoles = memberRoleRepository.findByMember(member);

    List<SimpleGrantedAuthority> grantedAuthorities = memberRoles.stream()
        .map(MemberRole::createGrantedAuthorities)
        .toList();

    return member.buildUser(grantedAuthorities);
  }
}
