package com.hv.community.backend.controller;


import com.hv.community.backend.dto.AccessTokenDto;
import com.hv.community.backend.dto.EmptyResponseDto;
import com.hv.community.backend.dto.JwtTokenDto;
import com.hv.community.backend.dto.TokenDto;
import com.hv.community.backend.dto.member.EmailActivateRequestDto;
import com.hv.community.backend.dto.member.EmailSendRequestDto;
import com.hv.community.backend.dto.member.EmailVerificationCodeDto;
import com.hv.community.backend.dto.member.ProfileResponseDto;
import com.hv.community.backend.dto.member.RefreshRequestDto;
import com.hv.community.backend.dto.member.SigninRequestDto;
import com.hv.community.backend.dto.member.SignupRequestDto;
import com.hv.community.backend.exception.MemberException;
import com.hv.community.backend.jwt.TokenProvider;
import com.hv.community.backend.service.mail.MailService;
import com.hv.community.backend.service.member.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;
  private final MailService mailService;
  private final TokenProvider tokenProvider;

  @PostMapping("/v1/signup")
  @Operation(summary = "Sign up", responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenDto.class)))
  })
  public ResponseEntity<TokenDto> signupV1(@RequestBody @Valid SignupRequestDto signupRequestDto) {
    TokenDto tokenDto = memberService.checkEmailDuplicationV1(signupRequestDto);
    return ResponseEntity.ok(tokenDto);
  }

  @PostMapping("/v1/email/send")
  @Operation(summary = "Email Send", responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmptyResponseDto.class)))
  })
  public ResponseEntity<EmptyResponseDto> emailSendV1(
      @RequestBody @Valid EmailSendRequestDto emailSendRequestDto) {
    String token = emailSendRequestDto.getToken();

    EmailVerificationCodeDto data = memberService.getEmailVerificationCodeV1(token);
    String email = data.getEmail();
    String verificationCode = data.getVerificationCode();

    mailService.sendEmailV1(email, "이메일 인증 메일 입니다", verificationCode);
    return ResponseEntity.ok(null);
  }

  @PostMapping("/v1/email/activate")
  @Operation(summary = "Email Activate", responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmptyResponseDto.class)))
  })
  public ResponseEntity<EmptyResponseDto> emailActivateV1(
      @RequestBody @Valid EmailActivateRequestDto emailActivateRequestDto) {
    memberService.emailActivateV1(emailActivateRequestDto);
    return ResponseEntity.ok(null);
  }

  @PostMapping("/v1/signin")
  @Operation(summary = "Sign in", responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtTokenDto.class)))
  })
  public ResponseEntity<JwtTokenDto> signinV1(
      @RequestBody @Valid SigninRequestDto signinRequestDto) {
    JwtTokenDto jwtTokenDto = memberService.signinV1(signinRequestDto.getEmail(),
        signinRequestDto.getPassword());
    return ResponseEntity.ok(jwtTokenDto);
  }

  @PostMapping("/v1/refresh")
  @Operation(summary = "Refresh Access Token", security = {
      @SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccessTokenDto.class)))
  })
  public ResponseEntity<AccessTokenDto> refreshV1(
      @RequestBody @Valid RefreshRequestDto refreshRequestDto) {
    // refresh token 유효성 검사
    String refreshToken = refreshRequestDto.getRefreshToken();
    if (!tokenProvider.validateToken(refreshToken)) {
      throw new MemberException("MEMBER:REFRESH_TOKEN_INVALID");
    }
    // refresh token으로 access token 재발급
    AccessTokenDto accessTokenDto = tokenProvider.refreshAccessToken(refreshToken);
    return ResponseEntity.ok(accessTokenDto);
  }

  @GetMapping("/v1/profile")
  @Operation(summary = "Get Profile", security = {
      @SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponseDto.class)))
  })
  public ResponseEntity<ProfileResponseDto> profileV1(@AuthenticationPrincipal User user) {
    // accesstoken 검사
    handleAuthorizationError(user);
    String email = getEmail(user);

    ProfileResponseDto profileResponseDto = memberService.profileV1(email);
    return ResponseEntity.ok(profileResponseDto);
  }

  private void handleAuthorizationError(User user) {
    if (user == null || user.getUsername() == null || user.getUsername().isEmpty()) {
      throw new MemberException("MEMBER:ACCESS_TOKEN_INVALID");
    }
  }

  private String getEmail(User user) {
    if (user == null) {
      return "";
    }
    return user.getUsername();
  }
}
