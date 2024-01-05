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

  private final String EMAIL_ACTIVATE_URL;
  private final String RESET_URL;
  private final MemberService memberService;
  private final MailService mailService;
  private final TokenProvider tokenProvider;

  public MemberController(MemberService memberService, MailService mailService,
      TokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
    String baseUrl = "http://localhost:5173";
    this.EMAIL_ACTIVATE_URL = baseUrl + "/user/confirm-email/";
    this.RESET_URL = baseUrl + "/user/reset-password/";
    this.memberService = memberService;
    this.mailService = mailService;
  }

  // POST /v1/signup
  // 회원가입 로직
  // email, nickname, password
  // token반환 및 verificationCode(6자리 숫자)메일로 발송

  // POST /v1/email/send
  // 이메일 재발송 로직
  // token
  // verificationCode(6자리 숫자)메일로 발송

  // POST /v1/email/activate
  // 이메일 활성화 로직
  // token, verificationCode

  // POST /v1/signin
  // 로그인
  // email, password
  // accessToken, refreshToken 반환

  // GET /v1/refresh
  // accessToken 재생성 로직
  // refreshToken
  // accessToken return

  // GET /v1/profile
  // accessToken
  // email, name return


  // POST /v1/signup
  // 회원가입 로직
  // email, nickname, password
  // token반환 및 verificationCode(6자리 숫자)메일로 발송
  @PostMapping("/v1/signup")
  public ResponseEntity<Object> signupV1(@RequestBody SignupRequestDto signupRequestDto,
      Errors errors) {
    new SignupRequestDtoValidator().validate(signupRequestDto, errors);

    String token = memberService.checkEmailDuplicationV1(signupRequestDto);
    Map<String, String> data = new HashMap<>();
    data.put("token", token);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(data);
  }

  // POST /v1/email/send
  // 이메일 재발송 로직
  // token
  // verificationCode(6자리 숫자)메일로 발송
  @PostMapping("/v1/email/send")
  public ResponseEntity emailSendV1(@RequestBody
  EmailSendRequestDto emailSendRequestDto) {
    String token = emailSendRequestDto.getToken();

    Map<String, String> data = memberService.getEmailVerificationCodeV1(token);
    String email = data.keySet().iterator().next();
    String verificationCode = data.get(email);

    mailService.sendEmailV1(email, "이메일 인증 메일 입니다", verificationCode);
    return ResponseEntity.ok(new HashMap<>());
  }

  // POST /v1/email/activate
  // 이메일 활성화 로직
  // token, verificationCode
  @PostMapping("/v1/email/activate")
  public ResponseEntity emailActivateV1(
      @RequestBody EmailActivateRequestDto emailActivateRequestDto) {
    memberService.emailActivateV1(emailActivateRequestDto);
    return ResponseEntity.ok(new HashMap<>());
  }

  // POST /v1/signin
  // 로그인
  // email, password
  // accessToken, refreshToken 반환
  @PostMapping("/v1/signin")
  public ResponseEntity signinV1(@RequestBody SigninRequestDto signinRequestDto,
      Errors errors) {
    new SigninRequestDtoValidator().validate(signinRequestDto, errors);

    TokenDto tokenDto = memberService.signinV1(signinRequestDto.getEmail(),
        signinRequestDto.getPassword());
    Map<String, String> data = new HashMap<>();
    data.put("access_token", tokenDto.getAccessToken());
    data.put("refresh_token", tokenDto.getRefreshToken());
    return ResponseEntity.ok(data);
  }

  // GET /v1/refresh
  // accessToken 재생성 로직
  // refreshToken
  // accessToken return
  @GetMapping("/v1/refresh")
  public ResponseEntity refreshV1(HttpServletRequest request) {
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

  // GET /v1/profile
  // accessToken
  // email, name return
  @GetMapping("/v1/profile")
  public ResponseEntity profileV1(@AuthenticationPrincipal User user) {
    // accesstoken검사
    handleAuthorizationError(user);
    String email = getEmail(user);

    Map<String, String> data = memberService.profileV1(email);
    return ResponseEntity.ok(data);
  }

//  // 비밀번호 초기화 메일보내기 로직
//  @PostMapping("/send-reset-password-email")
//  public ResponseEntity<ResponseDto> sendResetPasswordEmail(
//      @RequestBody SendResetPasswordEmailRequestDto sendResetPasswordEmailRequestDto,
//      Errors errors) {
//    new SendResetEmailRequestDtoValidator().validate(sendResetPasswordEmailRequestDto, errors);
//    if (errors.hasErrors()) {
//      return ResponseEntity.badRequest().body(
//          ResponseDto.builder().status("400").message("SEND_EMAIL_FAIL")
//              .errors(getErrorCode(errors)).build());
//    }
//
//    String email = sendResetPasswordEmailRequestDto.getEmail();
//    try {
//      String resetVerificationCode = memberService.getResetPasswordVerificationCode(email);
//      String resetVerificationUrl = RESET_URL + resetVerificationCode;
//      mailService.sendEmail(email, "이메일 인증 메일 입니다.", resetVerificationUrl);
//      return ResponseEntity.ok(
//          ResponseDto.builder().status("200").message("MAIL_SEND_SUCCESS").build());
//    } catch (RuntimeException e) {
//      Map<String, String> errorCode = new HashMap<>();
//      errorCode.put("email", "EMAIL_SEND_ERROR");
//      return ResponseEntity.internalServerError().body(
//          ResponseDto.builder().status("500").message("EMAIL_SEND_ERROR").errors(errorCode)
//              .build());
//    }
//  }
//
//  // 비밀번호 초기화 코드 검사
//  @GetMapping("/reset-password/{code}")
//  // 정상코드라면 이메일 리턴해서 보내준다. 리턴된 이메일은 프론트에서 defaultValue로 표시된다.
//  // 등록되지않은 코드라면 404 페이지를 표시
//  public ResponseEntity<ResponseDto> resetPasswordCode(@PathVariable String code) {
//    try {
//      String email = memberService.checkResetPasswordVerificationCode(code);
//      Map<String, String> responseData = new HashMap<>();
//      responseData.put("email", email);
//      return ResponseEntity.ok(
//          ResponseDto.builder().status("200").message("RESET_CODE_VALID").data(responseData)
//              .build());
//    } catch (RuntimeException e) {
//      Map<String, String> errorCode = new HashMap<>();
//      errorCode.put("code", "CODE_INVALUD");
//      return ResponseEntity.badRequest()
//          .body(ResponseDto.builder().status("400").message("RESET_CODE_INVALID").errors(errorCode)
//              .build());
//    }
//  }
//
//  // 비밀번호 초기화 (초기화 코드에서 요청을 보내준다)
//  @PostMapping("/reset-password")
//  public ResponseEntity<ResponseDto> resetPassword(
//      @RequestBody ResetPasswordRequestDto resetPasswordRequestDto, Errors errors) {
//    // 새로운 비밀번호 유효성검사
//    new ResetPasswordRequestDtoValidator().validate(resetPasswordRequestDto, errors);
//    if (errors.hasErrors()) {
//      return ResponseEntity.badRequest().body(
//          ResponseDto.builder().status("400").message("RESET_FAIL").errors(getErrorCode(errors))
//              .build());
//    }
//    try {
//      String code = resetPasswordRequestDto.getVerification_code();
//      String email = resetPasswordRequestDto.getEmail();
//      String newPassword = resetPasswordRequestDto.getPassword();
//      memberService.resetPassword(code, email, newPassword);
//      return ResponseEntity.ok(
//          ResponseDto.builder().status("200").message("SUCCESS_RESET_PASSWORD").build());
//    } catch (RuntimeException e) {
//      Map<String, String> errorCode = new HashMap<>();
//      errorCode.put("code", "CODE_INVALID");
//      return ResponseEntity.badRequest()
//          .body(
//              ResponseDto.builder().status("400").message("RESET_FAIL").errors(errorCode).build());
//    }
//  }

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
