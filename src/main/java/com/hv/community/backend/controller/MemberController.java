package com.hv.community.backend.controller;


import com.hv.community.backend.dto.ResponseDto;
import com.hv.community.backend.dto.ResponseErrorDto;
import com.hv.community.backend.dto.TokenDto;
import com.hv.community.backend.dto.member.ActivateEmailRequestDto;
import com.hv.community.backend.dto.member.ResetPasswordRequestDto;
import com.hv.community.backend.dto.member.ResetPasswordRequestDtoValidator;
import com.hv.community.backend.dto.member.SendEmailVerificationCodeRequestDto;
import com.hv.community.backend.dto.member.SendResetEmailRequestDtoValidator;
import com.hv.community.backend.dto.member.SendResetPasswordEmailRequestDto;
import com.hv.community.backend.dto.member.SigninRequestDto;
import com.hv.community.backend.dto.member.SigninRequestDtoValidator;
import com.hv.community.backend.dto.member.SignupRequestDto;
import com.hv.community.backend.dto.member.SignupRequestDtoValidator;
import com.hv.community.backend.jwt.TokenProvider;
import com.hv.community.backend.service.mail.MailService;
import com.hv.community.backend.service.member.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

  // POST signup
  // 회원가입 로직
  // email, nickname, password
  // token반환 및 verificationCode(6자리 숫자)메일로 발송

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

  // GET refreshAccessToken
  // accessToken 재생성로직
  // refreshToken
  // accessToken 반환

  // GET getMyProfile
  // email, name fetch
  // accessToken
  // email, name return


  // POST signup
  // 회원가입 로직
  // email, nickname, password
  // token반환 및 verificationCode(6자리 숫자)메일로 발송
  @PostMapping("/signup")
  public ResponseEntity signup(@RequestBody SignupRequestDto signupRequestDto,
      Errors errors) {
    new SignupRequestDtoValidator().validate(signupRequestDto, errors);
    if (errors.hasErrors()) {
      return ResponseEntity.badRequest().body(
          ResponseErrorDto.builder().id("MEMBER:VALIDATION_ERROR").message("이메일 유효성 검사를 통과하지 못했습니다")
              .build());

    }
    try {
      String token = memberService.signup(signupRequestDto);
      Map<String, String> data = new HashMap<>();
      data.put("token", token);
      return ResponseEntity.ok(data);
//      return ResponseEntity.ok(
//          ResponseDto.builder().status("200").message("SIGNUP_SUCCESS").data(data).build());
    } catch (RuntimeException e) {

      return ResponseEntity.badRequest().body(
          ResponseErrorDto.builder().id("MEMBER:SIGNUP_FAIL").message("이메일 등록에 실패했습니다"));
    }
  }

  // POST sendEmailVerificationCode
  // 이메일 재발송 로직
  // token
  // verificationCode(6자리 숫자)메일로 발송
  @PostMapping("/send-email-verification-code")
  public ResponseEntity sendEmailVerificationCode(@RequestBody
  SendEmailVerificationCodeRequestDto sendEmailVerificationCodeRequestDto) {

    String token = sendEmailVerificationCodeRequestDto.getToken();
    try {
      Map<String, String> data = memberService.getEmailVerificationCode(token);
      String email = data.keySet().iterator().next();
      String verificationCode = data.get(email);

      mailService.sendEmail(email, "이메일 인증 메일 입니다", verificationCode);
      return ResponseEntity.ok(
          ResponseDto.builder().status("200").message("SEND_EMAIL_SUCCESS").build());
    } catch (RuntimeException e) {
      return ResponseEntity.internalServerError().body(
          ResponseErrorDto.builder().id("MEMBER:SEND_EMAIL_NO_RESPONSE")
              .message("이메일 서버 연결 시간이 초과되었습니다").build());
    }
  }


  // POST activateEmail
  // 이메일 활성화 로직
  // token, verificationCode
  @PostMapping("/activate-email")
  public ResponseEntity activateEmail(
      @RequestBody ActivateEmailRequestDto activateEmailRequestDto) {
    try {
      memberService.activateEmail(activateEmailRequestDto);
      return ResponseEntity.ok(
          ResponseDto.builder().status("200").message("SUCCESS_ACTIVATE_EMAIL").build());
    } catch (RuntimeException e) {
      Map<String, String> errorCode = new HashMap<>();
      errorCode.put("code", "CODE_INVALID");
      return ResponseEntity.badRequest().body(
          ResponseErrorDto.builder().id("MEMBER:ACTIVATE_EMAIL_FAIL")
              .message("이메일 활성화에 실패했습니다").build());
    }
  }


  // POST signin
  // 로그인
  // email, password
  // accessToken, refreshToken 반환
  @PostMapping("/signin")
  public ResponseEntity signin(@RequestBody SigninRequestDto signinRequestDto,
      Errors errors) {
    // 이메일, 비밀번호 검사
    new SigninRequestDtoValidator().validate(signinRequestDto, errors);
    if (errors.hasErrors()) {
      return ResponseEntity.badRequest()
          .body(
              ResponseErrorDto.builder().id("MEMBER:SIGNIN_FAIL")
                  .message("로그인 유효성 검사를 통과하지 못했습니다"));
    }
    try {
      TokenDto tokenDto = memberService.signin(signinRequestDto.getEmail(),
          signinRequestDto.getPassword());
      Map<String, String> data = new HashMap<>();
      data.put("access_token", tokenDto.getAccessToken());
      data.put("refresh_token", tokenDto.getRefreshToken());
      return ResponseEntity.ok(data);
    } catch (RuntimeException e) {
      errors.rejectValue("email", "EMAIL_OR_PASSWORD_INVALID");
      errors.rejectValue("password", "EMAIL_OR_PASSWORD_INVALID");
      return ResponseEntity.badRequest().body(
          ResponseErrorDto.builder().id("MEMBER:EMAIL_OR_PASSWORD_INVALID")
              .message("이메일 혹은 비밀번호가 틀렸습니다"));
    }
  }

  // GET refreshAccessToken
  // accessToken 재생성로직
  // refreshToken
  // accessToken 반환
  @GetMapping("/refresh-access-token")
  public ResponseEntity refreshAccessToken(HttpServletRequest request) {
    try {
      // refresh token 유효성 검사
      String refreshToken = request.getHeader("Authorization").substring(7);
      if (!tokenProvider.validateToken(refreshToken)) {
        return ResponseEntity.badRequest().body(
            ResponseErrorDto.builder().id("MEMBER:REFRESH_TOKEN_INVALID")
                .message("리프레시토큰이 유효하지 않습니다").build());
      }

      // refresh token으로 access token 재발급
      String accessToken = tokenProvider.refreshAccessToken(refreshToken);

      Map<String, String> data = new HashMap<>();
      data.put("access_token", accessToken);
      return ResponseEntity.ok(data);
    } catch (RuntimeException e) {

      return ResponseEntity.badRequest()
          .body(ResponseErrorDto.builder().id("MEMBER:REFRESH_ACCESS_TOKEN_FAIL")
              .message("액세스토큰 갱신을 실패했습니다"));
    }
  }


  // GET getMyProfile
  // email, name fetch
  // accessToken
  // email, name return
  @GetMapping("/get-my-profile")
  public ResponseEntity getMyProfile(@AuthenticationPrincipal User user) {
    // accesstoken검사
    ResponseEntity<ResponseDto> authError = handleAuthorizationError(user);
    if (authError != null) {
      return authError;
    }

    String email = getEmail(user);
    try {
      String name = memberService.getMyProfile(email);
      Map<String, String> data = new HashMap<>();
      data.put("email", email);
      data.put("name", name);
      return ResponseEntity.ok(data);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(
          ResponseErrorDto.builder().id("MEMBER:GET_MY_PROFILE_FAIL").message("내 정보 가져오기에 실패했습니다")
              .build());
    }
  }


  // 비밀번호 초기화 메일보내기 로직
  @PostMapping("/send-reset-password-email")
  public ResponseEntity<ResponseDto> sendResetPasswordEmail(
      @RequestBody SendResetPasswordEmailRequestDto sendResetPasswordEmailRequestDto,
      Errors errors) {
    new SendResetEmailRequestDtoValidator().validate(sendResetPasswordEmailRequestDto, errors);
    if (errors.hasErrors()) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("SEND_EMAIL_FAIL")
              .errors(getErrorCode(errors)).build());
    }

    String email = sendResetPasswordEmailRequestDto.getEmail();
    try {
      String resetVerificationCode = memberService.getResetPasswordVerificationCode(email);
      String resetVerificationUrl = RESET_URL + resetVerificationCode;
      mailService.sendEmail(email, "이메일 인증 메일 입니다.", resetVerificationUrl);
      return ResponseEntity.ok(
          ResponseDto.builder().status("200").message("MAIL_SEND_SUCCESS").build());
    } catch (RuntimeException e) {
      Map<String, String> errorCode = new HashMap<>();
      errorCode.put("email", "EMAIL_SEND_ERROR");
      return ResponseEntity.internalServerError().body(
          ResponseDto.builder().status("500").message("EMAIL_SEND_ERROR").errors(errorCode)
              .build());
    }
  }

  // 비밀번호 초기화 코드 검사
  @GetMapping("/reset-password/{code}")
  // 정상코드라면 이메일 리턴해서 보내준다. 리턴된 이메일은 프론트에서 defaultValue로 표시된다.
  // 등록되지않은 코드라면 404 페이지를 표시
  public ResponseEntity<ResponseDto> resetPasswordCode(@PathVariable String code) {
    try {
      String email = memberService.checkResetPasswordVerificationCode(code);
      Map<String, String> responseData = new HashMap<>();
      responseData.put("email", email);
      return ResponseEntity.ok(
          ResponseDto.builder().status("200").message("RESET_CODE_VALID").data(responseData)
              .build());
    } catch (RuntimeException e) {
      Map<String, String> errorCode = new HashMap<>();
      errorCode.put("code", "CODE_INVALUD");
      return ResponseEntity.badRequest()
          .body(ResponseDto.builder().status("400").message("RESET_CODE_INVALID").errors(errorCode)
              .build());
    }
  }

  // 비밀번호 초기화 (초기화 코드에서 요청을 보내준다)
  @PostMapping("/reset-password")
  public ResponseEntity<ResponseDto> resetPassword(
      @RequestBody ResetPasswordRequestDto resetPasswordRequestDto, Errors errors) {
    // 새로운 비밀번호 유효성검사
    new ResetPasswordRequestDtoValidator().validate(resetPasswordRequestDto, errors);
    if (errors.hasErrors()) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("RESET_FAIL").errors(getErrorCode(errors))
              .build());
    }
    try {
      String code = resetPasswordRequestDto.getVerification_code();
      String email = resetPasswordRequestDto.getEmail();
      String newPassword = resetPasswordRequestDto.getPassword();
      memberService.resetPassword(code, email, newPassword);
      return ResponseEntity.ok(
          ResponseDto.builder().status("200").message("SUCCESS_RESET_PASSWORD").build());
    } catch (RuntimeException e) {
      Map<String, String> errorCode = new HashMap<>();
      errorCode.put("code", "CODE_INVALID");
      return ResponseEntity.badRequest()
          .body(
              ResponseDto.builder().status("400").message("RESET_FAIL").errors(errorCode).build());
    }
  }

  private ResponseEntity handleAuthorizationError(User user) {
    if (user == null || user.getUsername() == null || user.getUsername().isEmpty()) {
      Map<String, String> errorCode = new HashMap<>();
      errorCode.put("accessToken", "EMPTY_ACCESS_TOKEN");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ResponseErrorDto.builder().id("MEMBER:EMPTY_ACCESS_TOKEN").message("액세스토큰이 비어있습니다")
              .build());
    }
    return null;
  }

  private static Map<String, String> getErrorCode(Errors errors) {
    Map<String, String> errorCode = new HashMap<>();
    errors.getAllErrors()
        .forEach(error -> errorCode.put(((FieldError) error).getField(), error.getCode()));
    return errorCode;
  }

  private String getEmail(User user) {
    if (user == null) {
      return "";
    }
    return user.getUsername();
  }
}
