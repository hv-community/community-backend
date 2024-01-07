package com.hv.community.backend.service.member;


import com.hv.community.backend.domain.member.Member;
import com.hv.community.backend.repository.member.MemberRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Autowired
  public CustomUserDetailsService(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return memberRepository.findByEmail(email)
        .map(this::createUserDetails)
        .orElseThrow(() -> new UsernameNotFoundException(email + " -> 데이터베이스에서 찾을 수 없습니다."));
  }

  // DB 의 Member 값으로 UserDetails 객체 생성 후 반환
  private UserDetails createUserDetails(Member member) {
    log.info(member.toString());
    List<SimpleGrantedAuthority> grantedAuthorities = member.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
        .toList();

    return new User(
        member.getEmail(),
        member.getPassword(),
        grantedAuthorities
    );
  }
}
