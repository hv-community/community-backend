package com.hv.community.backend.controller;


import com.hv.community.backend.dto.TokenDto;
import com.hv.community.backend.dto.member.EmailActivateRequestDto;
import com.hv.community.backend.dto.member.EmailSendRequestDto;
import com.hv.community.backend.dto.member.SigninRequestDto;
import com.hv.community.backend.dto.member.SigninRequestDtoValidator;
import com.hv.community.backend.dto.member.SignupRequestDto;
import com.hv.community.backend.dto.member.SignupRequestDtoValidator;
import com.hv.community.backend.exception.MemberException;
import com.hv.community.backend.jwt.TokenProvider;
import com.hv.community.backend.service.mail.MailService;
import com.hv.community.backend.service.member.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
public class MemberController {

  private final MemberService memberService;
  private final MailService mailService;
  private final TokenProvider tokenProvider;

  public MemberController(MemberService memberService, MailService mailService,
      TokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
    this.memberService = memberService;
    this.mailService = mailService;
  }

  @PostMapping("/v1/signup")
  public ResponseEntity<Object> signupV1(@RequestBody SignupRequestDto signupRequestDto,
      Errors errors) {
    new SignupRequestDtoValidator().validate(signupRequestDto, errors);

    String token = memberService.checkEmailDuplicationV1(signupRequestDto);
    Map<String, String> data = new HashMap<>();
    data.put("token", token);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(data);
  }

  @PostMapping("/v1/email/send")
  public ResponseEntity<Map<String, Object>> emailSendV1(@RequestBody
  EmailSendRequestDto emailSendRequestDto) {
    String token = emailSendRequestDto.getToken();

    Map<String, String> data = memberService.getEmailVerificationCodeV1(token);
    String email = data.keySet().iterator().next();
    String verificationCode = data.get(email);

    mailService.sendEmailV1(email, "이메일 인증 메일 입니다", verificationCode);
    return ResponseEntity.ok(new HashMap<>());
  }

  @PostMapping("/v1/email/activate")
  public ResponseEntity<Map<String, Object>> emailActivateV1(
      @RequestBody EmailActivateRequestDto emailActivateRequestDto) {
    memberService.emailActivateV1(emailActivateRequestDto);
    return ResponseEntity.ok(new HashMap<>());
  }

  @PostMapping("/v1/signin")
  public ResponseEntity<Map<String, String>> signinV1(
      @RequestBody SigninRequestDto signinRequestDto,
      Errors errors) {
    new SigninRequestDtoValidator().validate(signinRequestDto, errors);

    TokenDto tokenDto = memberService.signinV1(signinRequestDto.getEmail(),
        signinRequestDto.getPassword());
    Map<String, String> data = new HashMap<>();
    data.put("access_token", tokenDto.getAccessToken());
    data.put("refresh_token", tokenDto.getRefreshToken());
    return ResponseEntity.ok(data);
  }

  @GetMapping("/v1/refresh")
  @Operation(security = {@SecurityRequirement(name = "bearer-key")})
  public ResponseEntity<Map<String, String>> refreshV1(HttpServletRequest request) {
    // refresh token 유효성 검사
    if (request.getHeader("Authorization") != null) {
      String refreshToken = request.getHeader("Authorization").substring(7);
      if (!tokenProvider.validateToken(refreshToken)) {
        throw new MemberException("MEMBER:REFRESH_TOKEN_INVALID");
      }
      // refresh token으로 access token 재발급
      String accessToken = tokenProvider.refreshAccessToken(refreshToken);

      Map<String, String> data = new HashMap<>();
      data.put("access_token", accessToken);
      return ResponseEntity.ok(data);
    } else {
      throw new MemberException("MEMBER:REFRESH_TOKEN_INVALID");
    }
  }

  @GetMapping("/v1/profile")
  @Operation(security = {@SecurityRequirement(name = "bearer-key")})
  public ResponseEntity<Map<String, String>> profileV1(@AuthenticationPrincipal User user) {
    // accesstoken검사
    handleAuthorizationError(user);
    String email = getEmail(user);

    Map<String, String> data = memberService.profileV1(email);
    return ResponseEntity.ok(data);
  }

  private void handleAuthorizationError(User user) {
    if (user == null || user.getUsername() == null || user.getUsername().isEmpty()) {
      throw new MemberException("MEMBER:EMPTY_ACCESS_TOKEN");
    }
  }

  private String getEmail(User user) {
    if (user == null) {
      return "";
    }
    return user.getUsername();
  }
}
