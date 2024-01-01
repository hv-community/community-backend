package com.hv.community.backend.controller;


import com.hv.community.backend.dto.ResponseDto;
import com.hv.community.backend.dto.TokenDto;
import com.hv.community.backend.dto.member.ActivateEmailRequestDto;
import com.hv.community.backend.dto.member.ActivateEmailRequestDtoValidator;
import com.hv.community.backend.dto.member.ResetPasswordRequestDto;
import com.hv.community.backend.dto.member.ResetPasswordRequestDtoValidator;
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

  // 회원가입
  @PostMapping("/signup")
  public ResponseEntity<ResponseDto> signup(@RequestBody SignupRequestDto signupRequestDto,
      Errors errors) {
    new SignupRequestDtoValidator().validate(signupRequestDto, errors);
    if (errors.hasErrors()) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("SIGNUP_FAIL").errors(getErrorCode(errors))
              .build());
    }
    try {
      TokenDto tokenDto = memberService.signup(signupRequestDto);
      Map<String, String> data = new HashMap<>();
      data.put("accessToken", tokenDto.getAccessToken());
      data.put("refreshToken", tokenDto.getRefreshToken());
      return ResponseEntity.ok(
          ResponseDto.builder().status("200").message("SIGNUP_SUCCESS").data(data).build());
    } catch (RuntimeException e) {
      errors.rejectValue("email", e.getMessage());
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("SIGNUP_FAIL").errors(getErrorCode(errors))
              .build());
    }
  }

  // 로그인 로직
  @PostMapping("/signin")
  public ResponseEntity<ResponseDto> signin(@RequestBody SigninRequestDto signinRequestDto,
      Errors errors) {
    // 이메일, 비밀번호 검사
    new SigninRequestDtoValidator().validate(signinRequestDto, errors);
    if (errors.hasErrors()) {
      return ResponseEntity.badRequest()
          .body(
              ResponseDto.builder().status("400").message("SIGNIN_FAIL")
                  .errors(getErrorCode(errors))
                  .build());
    }
    try {
      TokenDto tokenDto = memberService.signin(signinRequestDto.getEmail(),
          signinRequestDto.getPassword());
      Map<String, String> data = new HashMap<>();
      data.put("accessToken", tokenDto.getAccessToken());
      data.put("refreshToken", tokenDto.getRefreshToken());
      return ResponseEntity.ok(
          ResponseDto.builder().status("200").message("SIGNIN_SUCCESS").data(data).build());
    } catch (RuntimeException e) {
      errors.rejectValue("email", "EMAIL_OR_PASSWORD_INVALID");
      errors.rejectValue("password", "EMAIL_OR_PASSWORD_INVALID");
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("SIGNIN_FAIL").errors(getErrorCode(errors))
              .build());
    }
  }

  // accessToken 갱신 로직
  @GetMapping("/refresh-access-token")
  public ResponseEntity<ResponseDto> refreshAccessToken(HttpServletRequest request) {
    try {
      // refresh token 유효성 검사
      String refreshToken = request.getHeader("Authorization").substring(7);
      if (!tokenProvider.validateToken(refreshToken)) {
        return ResponseEntity.badRequest().build();
      }
      // refresh token으로 access token 재발급
      String accessToken = tokenProvider.refreshAccessToken(refreshToken);

      Map<String, String> data = new HashMap<>();
      data.put("accessToken", accessToken);
      return ResponseEntity.ok(
          ResponseDto.builder().status("200").message("REFRESH_ACCESS_TOKEN_SUCCESS").data(data)
              .build());
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
          .body(ResponseDto.builder().status("400").message("REFRESH_ACCESS_TOKEN_FAIL").build());
    }
  }

  // 이메일 인증이 되었는지 확인
  @GetMapping("/is-email-activate")
  public ResponseEntity<ResponseDto> isEmailActivated(@AuthenticationPrincipal User user) {
    // 로그인하지 않은 경우 - 401
    if (user == null) {
      Map<String, String> errorCode = new HashMap<>();
      errorCode.put("accessToken", "UNAUTHORIZED");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ResponseDto.builder().status("401").message("UNAUTHORIZED").errors(errorCode).build());
    }
    // 이메일 가져오기
    String email = getEmail(user);
    try {
      // 이메일 활성화여부 확인
      if (memberService.isVerifiedEmail(email)) {
        // 이메일 인증 O
        return ResponseEntity.ok(
            ResponseDto.builder().status("200").message("ALREADY_EMAIL_ACTIVATED").build());
      } else {
        // 이메일 인증 X
        return ResponseEntity.ok(
            ResponseDto.builder().status("200").message("EMAIL_NOT_ACTIVATED").build());
      }
    } catch (RuntimeException e) {
      // 이메일이 DB에 없는경우
      Map<String, String> errorCode = new HashMap<>();
      errorCode.put("email", "NOT_EXIST_EMAIL");
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("401").message("EMAIL_ERROR").errors(errorCode).build());
    }
  }


  // 메일관련 로직
  // 회원 활성화 이메일 발송 로직
  @GetMapping("/send-email-verification-code")
  public ResponseEntity<ResponseDto> sendEmailVerificationCode(@AuthenticationPrincipal User user) {
    if (user == null) {
      Map<String, String> errorCode = new HashMap<>();
      errorCode.put("accessToken", "UNAUTHORIZED");
      return new ResponseEntity<>(
          ResponseDto.builder().status("401").message("UNAUTHORIZED").errors(errorCode).build(),
          HttpStatus.UNAUTHORIZED);
    }

    String email = getEmail(user);
    try {
      if (memberService.isVerifiedEmail(email)) {
        Map<String, String> errorCode = new HashMap<>();
        errorCode.put("email", "ALREADY_EMAIL_ACTIVATED");
        return ResponseEntity.badRequest().body(
            ResponseDto.builder().status("400").message("ALREADY_EMAIL_ACTIVATED").errors(errorCode)
                .build());
      }
    } catch (RuntimeException e) {
      Map<String, String> errorCode = new HashMap<>();
      errorCode.put("email", "NOT_EXIST_EMAIL");
      return ResponseEntity.internalServerError().body(
          ResponseDto.builder().status("400").message("NOT_EXIST_EMAIL").errors(errorCode).build());
    }

    try {
      String activationCode = memberService.getEmailVerificationCode(email);
      String activationUrl = EMAIL_ACTIVATE_URL + activationCode;
      mailService.sendEmail(email, "이메일 인증 메일 입니다.", activationUrl);
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

  // 활성화 코드담긴 링크로 들어갔을때 링크가 유효한지 확인하고 리턴하는 로직
  @GetMapping("/check-email-verification-code/{code}")
  public ResponseEntity<ResponseDto> checkEmailVerificationCode(@PathVariable String code) {
    try {
      // 코드를 기반으로 이메일을 끌어온다
      String email = memberService.checkEmailVerificationCode(code);
      Map<String, String> responseDate = new HashMap<>();
      responseDate.put("email", email);
      // 200을 받으면 이메일을 리턴해주고 name, password, confirm password를 사용자가 입력하는 창이 뜨도록한다(최종 등록과정)
      return ResponseEntity.ok(
          ResponseDto.builder().status("200").message("SUCCESS_EMAIL_ACTIVATE").data(responseDate)
              .build());
    } catch (RuntimeException e) {
      // 코드가 정상적이지 않을경우 404 페이지 보여준다.
      Map<String, String> errorCode = new HashMap<>();
      errorCode.put("code", "CODE_INVALID");
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("EMAIL_ACTIVATE_FAIL").errors(errorCode)
              .build());
    }
  }

  // 요청받은 코드를 memberService의 메일 확인 로직을 실행한다.
  // memberService에서는 코드를 MemberTempRepository에서 code를 통한 검색을 진행한다.
  @PostMapping("/activate-email")
  public ResponseEntity<ResponseDto> activateEmail(
      @RequestBody ActivateEmailRequestDto activateEmailRequestDto, Errors errors) {
    // 들어온 dto 유효성검사 (비밀번호한정) > 파일만들어야함
    // 등록과정진행
    new ActivateEmailRequestDtoValidator().validate(activateEmailRequestDto, errors);
    if (errors.hasErrors()) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("ACTIVE_FAIL").errors(getErrorCode(errors))
              .build());
    }
    try {
      memberService.activateEmail(activateEmailRequestDto);
      return ResponseEntity.ok(
          ResponseDto.builder().status("200").message("SUCCESS_ACTIVATE_EMAIL").build());
    } catch (RuntimeException e) {
      Map<String, String> errorCode = new HashMap<>();
      errorCode.put("code", "CODE_INVALID");
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("ACTIVATE_FAIL").errors(errorCode).build());
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
      String code = resetPasswordRequestDto.getVerificationCode();
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
