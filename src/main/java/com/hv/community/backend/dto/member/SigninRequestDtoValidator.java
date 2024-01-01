package com.hv.community.backend.dto.member;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class SigninRequestDtoValidator implements Validator {

  private static final String EMAIL_REG_EXP = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
      + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
  private final Pattern pattern;

  public SigninRequestDtoValidator() {
    pattern = Pattern.compile(EMAIL_REG_EXP);
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return SigninRequestDto.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    SigninRequestDto signinRequestDto = (SigninRequestDto) target;

    // 이메일 유효성검사
    if (signinRequestDto.getEmail() == null || signinRequestDto.getEmail().trim().isEmpty()) {
      errors.rejectValue("email", "EMAIL_EMPTY");
    } else {
      Matcher matcher = pattern.matcher(signinRequestDto.getEmail());
      if (!matcher.matches()) {
        errors.rejectValue("email", "EMAIL_FORM_ERROR");
      }
    }
    // 비밀번호 검사
    if (signinRequestDto.getPassword() == null || signinRequestDto.getPassword().trim().isEmpty()) {
      errors.rejectValue("password", "PASSWORD_EMPTY");
    }
  }
}
