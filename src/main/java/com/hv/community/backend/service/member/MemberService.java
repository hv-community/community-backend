package com.hv.community.backend.service.member;


import com.hv.community.backend.domain.member.Member;
import com.hv.community.backend.domain.member.MemberTemp;
import com.hv.community.backend.domain.member.ResetVerificationCode;
import com.hv.community.backend.domain.member.Role;
import com.hv.community.backend.dto.TokenDto;
import com.hv.community.backend.dto.member.ActivateEmailRequestDto;
import com.hv.community.backend.dto.member.SignupRequestDto;
import com.hv.community.backend.jwt.TokenProvider;
import com.hv.community.backend.repository.member.MemberRepository;
import com.hv.community.backend.repository.member.MemberTempRepository;
import com.hv.community.backend.repository.member.ResetVerificationCodeRepository;
import com.hv.community.backend.util.Sha256;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
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
@Transactional
public class MemberService {

  private final MemberRepository memberRepository;
  private final MemberTempRepository memberTempRepository;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;
  private final TokenProvider tokenProvider;
  private final ResetVerificationCodeRepository resetVerificationCodeRepository;
  private final PasswordEncoder passwordEncoder;
  private final CustomUserDetailsService customUserDetailsService;


  public MemberService(MemberRepository memberRepository, MemberTempRepository memberTempRepository,
      AuthenticationManagerBuilder authenticationManagerBuilder, TokenProvider tokenProvider,
      ResetVerificationCodeRepository resetVerificationCodeRepository,
      PasswordEncoder passwordEncoder, CustomUserDetailsService customUserDetailsService) {
    this.memberRepository = memberRepository;
    this.memberTempRepository = memberTempRepository;
    this.authenticationManagerBuilder = authenticationManagerBuilder;
    this.tokenProvider = tokenProvider;
    this.resetVerificationCodeRepository = resetVerificationCodeRepository;
    this.passwordEncoder = passwordEncoder;
    this.customUserDetailsService = customUserDetailsService;
  }

  // 회원가입 로직
  public TokenDto signup(SignupRequestDto signupRequestDto) {
    if (memberRepository.existsByEmail(signupRequestDto.getEmail())) {

      log.debug("중복 이메일 가입 요청, 요청 이메일: {}", signupRequestDto.getEmail());

      throw new RuntimeException("EMAIL_EXIST1");
    }

    String temporaryPassword = passwordEncoder.encode(signupRequestDto.getEmail() + new Date());
    try {
      Role roles = new Role();
      roles.setRoleName("ROLE_NONE");

      Member member = new Member();
      member.setEmail(signupRequestDto.getEmail());
      member.setPassword(passwordEncoder.encode(temporaryPassword));
      member.setRegisterDate(new Date());
      member.setRoles(Collections.singleton(roles));
      member.setEmailActivated(0);

      memberRepository.save(member);

      // 1. Login Email, Password 로 AuthenticationToken 생성
      UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
          member.getEmail(), temporaryPassword);

      // 2. Email, Password 일치 검증이 일어남
      //    authenticationManagerBuilder.getObject().authenticate() 에서 CustomUserDetailsService 의 loadUserByUsername() 실행됨
      Authentication authentication = authenticationManagerBuilder.getObject()
          .authenticate(authenticationToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);

      // 3. 인증 정보로 JWT 토큰 생성
      TokenDto tokenDto = tokenProvider.createToken(authentication);

      // 4. 토큰 발급
      return tokenDto;
    } catch (Exception e) {
      log.debug("signup오류 발생", e);
      throw new RuntimeException("SIGNUP_FAIL");
    }
  }

  // 이메일 확인 인증 코드 생성로직
  public String createEmailVerificationCode(String email) {
    String verificationCode = "Create Error";
    try {
      for (int i = 1; i <= 5; ++i) {
        verificationCode = Sha256.encrypt(email);
        if (!memberTempRepository.existsByCode(verificationCode)) {
          break;
        }
      }
    } catch (NoSuchAlgorithmException e) {
      verificationCode = "Create Error";
    }

    // save db
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("미가입 이메일 인증 요청"));

    MemberTemp memberTemp = new MemberTemp();
    memberTemp.setMember(member);
    memberTemp.setCode(verificationCode);
    memberTempRepository.save(memberTemp);
    return verificationCode;
  }

  // 이메일 확인 인증 코드 가져오는 로직
  public String getEmailVerificationCode(String email) {
    Member member = memberRepository.findByEmail(email).orElse(null);
    if (member == null || member.getMemberTemp() == null) {
      return createEmailVerificationCode(email);
    }
    return member.getMemberTemp().getCode();
  }

  // 이메일 확인 인증 코드를 통해 이메일을 리턴하는 로직
  public String checkEmailVerificationCode(String code) {
    MemberTemp memberTemp = memberTempRepository.findByCode(code)
        .orElseThrow(() -> new RuntimeException("잘못된 인증 code 인증 시도"));
    return memberTemp.getMember().getEmail();
  }

  // 인증된 메일인지 확인하는 로직
  @Transactional(readOnly = true)
  public boolean isVerifiedEmail(String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("미가입 이메일 인증 요청"));
    return member.getEmailActivated() == 1;
  }

  // 이메일 인증확인과 name, password 등록 로직
  public boolean activateEmail(ActivateEmailRequestDto activateEmailRequestDto) {
    String verificationCode = activateEmailRequestDto.getVerificationCode();
    String email = activateEmailRequestDto.getEmail();
    String name = activateEmailRequestDto.getName();
    String password = activateEmailRequestDto.getPassword();

    MemberTemp memberTemp = memberTempRepository.findByCode(verificationCode)
        .orElseThrow(() -> new RuntimeException("잘못된 code 인증 시도"));
    if (Objects.equals(email, memberTemp.getMember().getEmail())) {
      Member member = memberTemp.getMember();
      member.setNickname(name);
      member.setPassword(passwordEncoder.encode(password));
      member.setEmailActivated(1);

      memberTempRepository.delete(memberTemp);
      return true;
    } else {
      return false;
    }
  }

  // 로그인 로직
  @Transactional(readOnly = true)
  public TokenDto signin(String email, String password) {

    // 1. Login Email, Password 로 AuthenticationToken 생성
    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
        email, password);

    // 2. Email, Password 일치 검증이 일어남
    //    authenticationManagerBuilder.getObject().authenticate() 에서 CustomUserDetailsService 의 loadUserByUsername() 실행됨
    Authentication authentication = authenticationManagerBuilder.getObject()
        .authenticate(authenticationToken);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // 3. 인증 정보로 JWT 토큰 생성
    TokenDto tokenDto = tokenProvider.createToken(authentication);

    // 4. 토큰 발급
    return tokenDto;
  }

  // 비밀번호 초기화 코드 생성 로직
  public String createResetPasswordVerificationCode(String email) {
    String verificationCode = "Create Error";

    try {
      for (int i = 1; i <= 5; ++i) {
        verificationCode = Sha256.encrypt(email);
        if (!resetVerificationCodeRepository.existsByCode(verificationCode)) {
          break;
        }
      }
    } catch (NoSuchAlgorithmException e) {
      verificationCode = "Create Error";
    }

    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("미가입 이메일 인증 요청;"));
    ResetVerificationCode resetVerificationCode = new ResetVerificationCode();
    resetVerificationCode.setMember(member);
    resetVerificationCode.setCode(verificationCode);
    resetVerificationCodeRepository.save(resetVerificationCode);
    return verificationCode;
  }

  // 비밀번호 초기화 코드 가져오는 로직
  public String getResetPasswordVerificationCode(String email) {
    Member member = memberRepository.findByEmail(email).orElse(null);
    if (member == null || member.getResetVerificationCode() == null) {
      return createResetPasswordVerificationCode(email);
    }
    return member.getResetVerificationCode().getCode();
  }

  // 비밀번호 초기화 코드를 확인하고 이메일을 리턴해서 프론트의 defaultValue값을 채워주는 로직
  public String checkResetPasswordVerificationCode(String code) {
    ResetVerificationCode resetVerificationCode = resetVerificationCodeRepository.findByCode(code)
        .orElseThrow(() -> new RuntimeException("잘못된 인증 code 인증 시도"));

    Member member = resetVerificationCode.getMember();
    return member.getEmail();
  }

  // 비밀번호 초기화 코드와 새로운 비밀번호를 가져와서 Member의 새로운 비밀번호를 넣어주는 로직
  @Transactional
  public boolean resetPassword(String code, String email, String newPassword) {
    ResetVerificationCode resetVerificationCode = resetVerificationCodeRepository.findByCode(code)
        .orElseThrow(() -> new RuntimeException("잘못된 인증 code 인증 시도"));
    if (Objects.equals(email, resetVerificationCode.getMember().getEmail())) {
      Member member = resetVerificationCode.getMember();
      member.setPassword(passwordEncoder.encode(newPassword));
      memberRepository.save(member);
      resetVerificationCodeRepository.delete(resetVerificationCode);
      return true;
    } else {
      return false;
    }
  }
}
