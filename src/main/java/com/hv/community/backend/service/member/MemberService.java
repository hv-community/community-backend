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
  private final PasswordEncoder passwordEncoder;
  private final MailService mailService;
  private final RoleRepository roleRepository;
  private final MemberRoleRepository memberRoleRepository;
  private static String unregistered = "MEMBER:MEMBER_UNREGISTERED";


  public MemberService(MemberRepository memberRepository, MemberTempRepository memberTempRepository,
      AuthenticationManagerBuilder authenticationManagerBuilder, TokenProvider tokenProvider,
      PasswordEncoder passwordEncoder, MailService mailService, RoleRepository roleRepository,
      MemberRoleRepository memberRoleRepository) {
    this.memberRepository = memberRepository;
    this.memberTempRepository = memberTempRepository;
    this.authenticationManagerBuilder = authenticationManagerBuilder;
    this.tokenProvider = tokenProvider;
    this.passwordEncoder = passwordEncoder;
    this.mailService = mailService;
    this.roleRepository = roleRepository;
    this.memberRoleRepository = memberRoleRepository;
  }


  public TokenDto checkEmailDuplicationV1(SignupRequestDto signupRequestDto) {
    // 해당 이메일이 존재하는경우
    // 토큰값이 있는가?
    // 토큰이 있다면 생성일로부터 24시간이 지났는가?
    // 토큰생성으로 24시간이 지났다면 새로운 토큰을 만들고 새롭게 저장한다
    // 토큰생성 24시간이 되지않았다면 중복 이메일 가입 요청으로 리턴시킴
    Member isMember = memberRepository.findByEmail(signupRequestDto.getEmail()).orElse(null);
    // 같은 이메일이 존재하는가?
    if (isMember != null) {
      // 이메일이 존재할때 토큰값이 비어있는가?
      String token = isMember.getToken();
      if (token == null) {
        // 이미 인증완료된 유저
        log.debug("중복 이메일 가입 요청, 요청 이메일: {}", signupRequestDto.getEmail());
        throw new MemberException("MEMBER:MAIL_EXIST");
      } else {
        // 토큰값이 24시간이 지났는가?
        Date storedTime = isMember.getRegisterDate();
        Date currentTime = new Date();

        long timeDifferenceMillis = currentTime.getTime() - storedTime.getTime();
        long twentyFourHoursMillis = (long) 24 * 60 * 60 * 1000;
        boolean is24HoursPassed = timeDifferenceMillis > twentyFourHoursMillis;
        // 24시간 지났다면 해당유저 지우고 다시등록
        if (is24HoursPassed) {
          cleanUpUser(isMember);
          return signupV1(signupRequestDto);
        } else {
          // 24시간동안 중복가입시도 금지
          log.debug("중복 이메일 가입 요청, 요청 이메일: {}", signupRequestDto.getEmail());
          throw new MemberException("MEMBER:MAIL_EXIST");
        }
      }
      // 같은 이메일이 존재하지않을때는 닉네임 중복검사
    } else if (memberRepository.existsByNickname(signupRequestDto.getNickname())) {
      log.debug("중복 닉네임 가입 요청, 요청 닉네임: {}", signupRequestDto.getEmail());
      throw new MemberException("MEMBER:NICKNAME_EXIST");
    } else {
      return signupV1(signupRequestDto);
    }
  }

  @Transactional
  public void cleanUpUser(Member member) {
    // 기존 유저 정보 삭제
    memberTempRepository.delete(member.getMemberTemp());
    memberRoleRepository.deleteByMember(member);
    memberRepository.delete(member);
    memberRepository.flush();
  }

  private TokenDto signupV1(SignupRequestDto signupRequestDto) {
    String token;
    try {
      Role role = new Role();
      role.setRoleName("ROLE_NONE");
      roleRepository.save(role);
      Member member = new Member();
      member.setEmail(signupRequestDto.getEmail());
      member.setNickname(signupRequestDto.getNickname());
      member.setPassword(passwordEncoder.encode(signupRequestDto.getPassword()));
      member.setRegisterDate(new Date());
      member.setEmailActivated(0);
      // 토큰생성후 리턴
      token = passwordEncoder.encode(
          member.getEmail() + member.getNickname() + member.getRegisterDate());
      member.setToken(token);
      memberRepository.save(member);

      // jwt role
      MemberRole memberRole = new MemberRole();
      memberRole.setMember(member);
      memberRole.setRole(role);
      memberRoleRepository.save(memberRole);
    } catch (Exception e) {
      throw new MemberException("MEMBER:SIGNUP_FAIL");
    }

    // 이메일 활성화 코드 메일 발송
    String verificationCode = createEmailVerificationCodeV1(token);
    mailService.sendEmailV1(signupRequestDto.getEmail(), "이메일 인증 코드입니다.", verificationCode);
    return TokenDto.builder()
        .token(token)
        .build();
  }


  private String generateRandomNumericCodeV1(int length) {
    Random random = new SecureRandom();
    StringBuilder code = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int digit = random.nextInt(10);
      code.append(digit);
    }
    return code.toString();
  }

  public String createEmailVerificationCodeV1(String token) {
    Member member = memberRepository.findByToken(token)
        .orElseThrow(() -> new MemberException(unregistered));
    try {
      String verificationCode = generateRandomNumericCodeV1(6);
      MemberTemp memberTemp = new MemberTemp();
      memberTemp.setMember(member);
      memberTemp.setToken(token);
      memberTemp.setCode(verificationCode);
      memberTempRepository.save(memberTemp);
      return verificationCode;
    } catch (Exception e) {
      throw new MemberException("MEMBER:CREATE_EMAIL_VERIFICATION_CODE_FAIL");
    }
  }

  public EmailVerificationCodeDto getEmailVerificationCodeV1(String token) {
    Member member = memberRepository.findByToken(token)
        .orElseThrow(() -> new MemberException(unregistered));
    try {
      if (member == null || member.getMemberTemp() == null) {
        assert member != null;
        return EmailVerificationCodeDto.builder()
            .email(member.getEmail())
            .verificationCode(createEmailVerificationCodeV1(token))
            .build();
      }
      return EmailVerificationCodeDto.builder()
          .email(member.getEmail())
          .verificationCode(member.getMemberTemp().getCode())
          .build();
    } catch (Exception e) {
      throw new MemberException("MEMBER:GET_EMAIL_VERIFICATION_CODE_FAIL");
    }
  }

  public void emailActivateV1(EmailActivateRequestDto emailActivateRequestDto) {
    String token = emailActivateRequestDto.getToken();
    String verificationCode = emailActivateRequestDto.getVerificationCode();

    Member member = memberRepository.findByToken(token)
        .orElseThrow(() -> new MemberException(unregistered));
    try {
      if (Objects.equals(verificationCode, member.getMemberTemp().getCode())) {
        member.setEmailActivated(1);
        member.setToken(null);
        memberRepository.save(member);
        memberTempRepository.delete(member.getMemberTemp());
      } else {
        throw new MemberException("MEMBER:ACTIVATE_EMAIL_FAIL");
      }
    } catch (Exception e) {
      throw new MemberException("MEMBER:ACTIVATE_EMAIL_FAIL");
    }
  }

  @Transactional(readOnly = true)
  public JwtTokenDto signinV1(String email, String password) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new MemberException(unregistered));
    if (member.getEmailActivated() == 1) {
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
        .orElseThrow(() -> new MemberException(unregistered));
    try {
      return ProfileResponseDto.builder()
          .id(String.valueOf(member.getId()))
          .email(member.getEmail())
          .nickname(member.getNickname())
          .build();
    } catch (Exception e) {
      throw new MemberException("MEMBER:GET_MY_PROFILE_ERROR");
    }
  }
}
