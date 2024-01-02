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
import com.hv.community.backend.repository.member.RoleRepository;
import com.hv.community.backend.service.mail.MailService;
import com.hv.community.backend.util.Sha256;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
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
  private final MailService mailService;
  private final RoleRepository roleRepository;


  public MemberService(MemberRepository memberRepository, MemberTempRepository memberTempRepository,
      AuthenticationManagerBuilder authenticationManagerBuilder, TokenProvider tokenProvider,
      ResetVerificationCodeRepository resetVerificationCodeRepository,
      PasswordEncoder passwordEncoder, CustomUserDetailsService customUserDetailsService,
      MailService mailService, RoleRepository roleRepository) {
    this.memberRepository = memberRepository;
    this.memberTempRepository = memberTempRepository;
    this.authenticationManagerBuilder = authenticationManagerBuilder;
    this.tokenProvider = tokenProvider;
    this.resetVerificationCodeRepository = resetVerificationCodeRepository;
    this.passwordEncoder = passwordEncoder;
    this.customUserDetailsService = customUserDetailsService;
    this.mailService = mailService;
    this.roleRepository = roleRepository;
  }

  // POST signup
  // 회원가입 로직
  // email, nickname, password
  // token반환 및 verificationCode(6자리 숫자)메일로 발송

  // generateRandomNumericCode
  // n자리 랜덤 번호생성

  // createEmailVerificationCode
  // n자리 랜던 번호를 받아서 memberTemp에 토큰과 같이 저장

  // getEmailVerificationCode
  // 만들어진 코드가 있는지 확인하고 없으면 createEmailVerificationCode실행

  // POST sendEmailVerificationCode
  // 이메일 재발송 로직
  // token
  // verificationCode(6자리 숫자)메일로 발송

  // POST activateEmail
  // 이메일 활성화 로직
  // token, verificationCode

  // POST signin
  // 로그인
  // email, password
  // accessToken, refreshToken 반환

  // GET getMyProfile
  // email, name fetch
  // accessToken
  // email, name return

  // POST signup
  // 회원가입 로직
  // email, nickname, password
  // token반환 및 verificationCode(6자리 숫자)메일로 발송
  public String signup(SignupRequestDto signupRequestDto) {
    if (memberRepository.existsByEmail(signupRequestDto.getEmail())) {
      log.debug("중복 이메일 가입 요청, 요청 이메일: {}", signupRequestDto.getEmail());
      throw new RuntimeException("EMAIL_EXIST");
    } else if (memberRepository.existsByNickname(signupRequestDto.getNickname())) {
      log.debug("중복 닉네임 가입 요청, 요청 닉네임: {}", signupRequestDto.getEmail());
      throw new RuntimeException("NICKNAME_EXIST");
    }
    Role role = new Role();
    role.setRoleName("ROLE_NONE");
    roleRepository.save(role);


    Member member = new Member();
    member.setEmail(signupRequestDto.getEmail());
    member.setNickname(signupRequestDto.getNickname());
    member.setPassword(passwordEncoder.encode(signupRequestDto.getPassword()));
    member.setRegisterDate(new Date());

    member.setRoles(Collections.singleton(role));
    member.setEmailActivated(0);

    // 토큰생성후 리턴
    String token = passwordEncoder.encode(
        member.getEmail() + member.getNickname() + member.getRegisterDate());
    member.setToken(token);
    memberRepository.save(member);

    // 이메일 활성화 코드 메일 발송
    String verificationCode = createEmailVerificationCode(token);
    // 이메일 발송중 에러리턴하는경우 token을 못받는 문제
    mailService.sendEmail(signupRequestDto.getEmail(), "이메일 인증 코드입니다.", verificationCode);

    return token;
  }

  // generateRandomNumericCode
  // n자리 랜덤 번호생성
  private String generateRandomNumericCode(int length) {
    Random random = new SecureRandom();
    StringBuilder code = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int digit = random.nextInt(10);
      code.append(digit);
    }
    return code.toString();
  }

  // createEmailVerificationCode
  // n자리 랜던 번호를 받아서 memberTemp에 토큰과 같이 저장
  public String createEmailVerificationCode(String token) {
    // 이메일 활성화 코드 생성
    Member member = memberRepository.findByToken(token);
    String verificationCode = generateRandomNumericCode(6);
    MemberTemp memberTemp = new MemberTemp();
    memberTemp.setMember(member);
    memberTemp.setToken(token);
    memberTemp.setCode(verificationCode);
    memberTempRepository.save(memberTemp);

    return verificationCode;
  }

  // getEmailVerificationCode
  // 만들어진 코드가 있는지 확인하고 없으면 createEmailVerificationCode실행
  public Map<String, String> getEmailVerificationCode(String token) {
    Member member = memberRepository.findByToken(token);
    if (member == null || member.getMemberTemp() == null) {
      Map<String, String> data = new HashMap<>();
      assert member != null;
      data.put(member.getEmail(), createEmailVerificationCode(token));
      return data;
    }
    Map<String, String> data = new HashMap<>();
    data.put(member.getEmail(), member.getMemberTemp().getCode());
    return data;
  }


  // POST activateEmail
  // 이메일 활성화 로직
  // token, verificationCode
  public void activateEmail(ActivateEmailRequestDto activateEmailRequestDto) {
    String token = activateEmailRequestDto.getToken();
    String verificationCode = activateEmailRequestDto.getVerification_code();

    MemberTemp memberTemp = memberTempRepository.findByCode(verificationCode)
        .orElseThrow(() -> new RuntimeException("잘못된 code 인증 시도"));
    if (Objects.equals(token, memberTemp.getMember().getToken())) {
      Member member = memberTemp.getMember();
      member.setEmailActivated(1);
      memberTempRepository.delete(memberTemp);
    }
  }

  // POST signin
  // 로그인
  // email, password
  // accessToken, refreshToken 반환
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

  // GET getMyProfile
  // email, name fetch
  // accessToken
  // email, name return
  public String getMyProfile(String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("해당하는 유저가 없습니다."));
    return member.getNickname();
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
