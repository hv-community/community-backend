package com.hv.community.backend.service.member;


import com.hv.community.backend.domain.member.Member;
import com.hv.community.backend.domain.member.MemberRole;
import com.hv.community.backend.domain.member.MemberTemp;
import com.hv.community.backend.domain.member.Role;
import com.hv.community.backend.dto.JwtTokenDto;
import com.hv.community.backend.dto.TokenDto;
import com.hv.community.backend.dto.member.EmailActivateRequestDto;
import com.hv.community.backend.dto.member.EmailVerificationCodeDto;
import com.hv.community.backend.dto.member.ProfileResponseDto;
import com.hv.community.backend.dto.member.SignupRequestDto;
import com.hv.community.backend.exception.MemberException;
import com.hv.community.backend.jwt.TokenProvider;
import com.hv.community.backend.repository.member.MemberRepository;
import com.hv.community.backend.repository.member.MemberRoleRepository;
import com.hv.community.backend.repository.member.MemberTempRepository;
import com.hv.community.backend.repository.member.RoleRepository;
import com.hv.community.backend.service.mail.MailService;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final MemberTempRepository memberTempRepository;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;
  private final TokenProvider tokenProvider;
  private final PasswordEncoder passwordEncoder;
  private final MailService mailService;
  private final RoleRepository roleRepository;
  private final MemberRoleRepository memberRoleRepository;
  private static final String MEMBER_UNREGISTERED = "MEMBER:MEMBER_UNREGISTERED";

  // 중복체크 시작
  public TokenDto checkEmailDuplicationV1(SignupRequestDto signupRequestDto) {
    Member existingMember = memberRepository.findByEmail(signupRequestDto.getEmail()).orElse(null);
    if (existingMember != null) {
      // 이메일로 이미 가입된 유저가 있을때
      return handleExistingMember(existingMember, signupRequestDto);
    } else {
      // 이메일로 이미 가입된 유저가 없을때
      return handleSignUpMember(signupRequestDto);
    }
  }

  private TokenDto handleExistingMember(Member existingMember, SignupRequestDto signupRequestDto) {
    // 토큰값 확인
    // 토큰값 비어있다 => 인증완료되어 이미 가입완료된 상태
    if (existingMember.checkTokenNull()) {
      log.debug("중복 이메일 가입 요청, 요청 이메일: {}", signupRequestDto.getEmail());
      throw new MemberException("MEMBER:MAIL_EXIST");
    }
    // 토큰값 존재하는경우 => 아직 인증되지않은 상태
    // 24시간이 지나서 토큰이 만료되었는지 확인
    // 토큰이 만료된 경우 해당 유저를 지우고 재가입 로직 진행
    if (existingMember.checkTokenExpired()) {
      return handleTokenExpired(existingMember, signupRequestDto);
    }
    // 토큰이 만료되지 않은경우 중복가입시도 금지
    log.debug("중복 이메일 가입 요청, 요청 이메일: {}", signupRequestDto.getEmail());
    throw new MemberException("MEMBER:MAIL_EXIST");
  }

  private TokenDto handleTokenExpired(Member existingMember, SignupRequestDto signupRequestDto) {
    // 기존 유저 정보 삭제
    existingMember.cleanUpUser(memberTempRepository, memberRoleRepository, memberRepository);
    memberRepository.flush();
    // 닉네임 중복조회 -> 가입절차 진행
    return handleSignUpMember(signupRequestDto);
  }

  private TokenDto handleSignUpMember(SignupRequestDto signupRequestDto) {
    // 이메일로 가입된 유저가 없을때
    // 닉네임으로 조회
    // 닉네임으로 가입된 유저가 있을때
    if (memberRepository.existsByNickname(signupRequestDto.getNickname())) {
      log.debug("중복 닉네임 가입 요청, 요청 닉네임: {}", signupRequestDto.getNickname());
      throw new MemberException("MEMBER:NICKNAME_EXIST");
    }
    // 이메일, 닉네임 중복없을때 실행
    return signupV1(signupRequestDto);
  }
  // 중복체크 끝 -> 이후 가입로직 진행

  // 가입로직 시작
  private TokenDto signupV1(SignupRequestDto signupRequestDto) {
    Role role = Role.builder()
        .roleName("ROLE_NONE")
        .build();
    roleRepository.save(role);

    // email, nickname, registerDate 를 사용하여 토큰생성
    String email = signupRequestDto.getEmail();
    String nickname = signupRequestDto.getNickname();
    String password = passwordEncoder.encode(signupRequestDto.getPassword());
    Date registerDate = new Date();
    String token = UUID.randomUUID().toString();

    Member member = Member.builder()
        .email(email)
        .nickname(nickname)
        .password(password)
        .registerDate(registerDate)
        .emailActivated(0)
        .token(token)
        .build();
    memberRepository.save(member);

    // jwt role
    MemberRole memberRole = MemberRole.builder()
        .member(member)
        .role(role)
        .build();
    memberRoleRepository.save(memberRole);

    String verificationCode = createEmailVerificationCodeV1(token);
    mailService.sendEmailV1(signupRequestDto.getEmail(), "이메일 인증 코드입니다.", verificationCode);
    return TokenDto.builder()
        .token(token)
        .build();
  }

  private String createEmailVerificationCodeV1(String token) {
    Member member = memberRepository.findByToken(token)
        .orElseThrow(() -> new MemberException(MEMBER_UNREGISTERED));
    String verificationCode = generateRandomNumericCodeV1();
    MemberTemp memberTemp = MemberTemp.builder()
        .token(token)
        .code(verificationCode)
        .member(member)
        .build();
    memberTempRepository.save(memberTemp);
    return verificationCode;
  }

  private String generateRandomNumericCodeV1() {
    Random random = new SecureRandom();
    StringBuilder code = new StringBuilder(6);
    for (int i = 0; i < 6; i++) {
      int digit = random.nextInt(10);
      code.append(digit);
    }
    return code.toString();
  }
  // 가입로직 끝

  // email resend
  public EmailVerificationCodeDto getEmailVerificationCodeV1(String token) {
    Member member = memberRepository.findByToken(token)
        .orElseThrow(() -> new MemberException(MEMBER_UNREGISTERED));
    return member.buildEmailVerificationCodeDto();
  }

  public void emailActivateV1(EmailActivateRequestDto emailActivateRequestDto) {
    String token = emailActivateRequestDto.getToken();
    String verificationCode = emailActivateRequestDto.getVerificationCode();

    Member member = memberRepository.findByToken(token)
        .orElseThrow(() -> new MemberException(MEMBER_UNREGISTERED));
    member.emailActivate(verificationCode, memberRepository, memberTempRepository);
  }

  public JwtTokenDto signinV1(String email, String password) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new MemberException(MEMBER_UNREGISTERED));
    if (member.checkEmailActivated()) {
      try {
        // 1. Login Email, Password 로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            email, password);

        // 2. Email, Password 일치 검증이 일어남
        //    authenticationManagerBuilder.getObject().authenticate() 에서 CustomUserDetailsService 의 loadUserByUsername() 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject()
            .authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. 인증 정보로 JWT 토큰 생성
        // 4. 토큰 발급
        return tokenProvider.createToken(authentication);
      } catch (Exception e) {
        throw new MemberException("MEMBER:EMAIL_OR_PASSWORD_ERROR");
      }
    } else {
      throw new MemberException("MEMBER:EMAIL_ACTIVATE_REQUIRE");
    }
  }

  public ProfileResponseDto profileV1(String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new MemberException(MEMBER_UNREGISTERED));
    return member.buildProfileResponseDto();
  }
}
