package com.hv.community.backend.dto.member;

import com.hv.community.backend.exception.MemberException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class SignupRequestDtoValidator implements Validator {

  private static final String EMAIL_REG_EXP = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
      + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
  private final Pattern pattern;

  public SignupRequestDtoValidator() {
    pattern = Pattern.compile(EMAIL_REG_EXP);
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return SignupRequestDto.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    SignupRequestDto signupRequestDto = (SignupRequestDto) target;

    // 이메일 유효성검사
    if (signupRequestDto.getEmail() == null || signupRequestDto.getEmail().trim().isEmpty()) {
      throw new MemberException("MEMBER:EMAIL_EMPTY");
    } else {
      Matcher matcher = pattern.matcher(signupRequestDto.getEmail());
      if (!matcher.matches()) {
        throw new MemberException("MEMBER:EMAIL_FORM_ERROR");
      }
    }
    if (signupRequestDto.getNickname().length() < 4 ||
        signupRequestDto.getNickname().length() > 20) {
      throw new MemberException("MEMBER:NICKNAME_FORM_ERROR");
    }
    if (signupRequestDto.getPassword().length() < 8 ||
        signupRequestDto.getPassword().length() > 20 ||
        !containsSpecialCharacter(signupRequestDto.getPassword())) {
      throw new MemberException("MEMBER:PASSWORD_FORM_ERROR");
    }
  }

  // 특수문자 포함 여부를 정규식으로 확인
  private boolean containsSpecialCharacter(String password) {
    return password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>/?]+.*");
  }
}
