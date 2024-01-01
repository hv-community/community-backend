package com.hv.community.backend.dto.member;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ResetPasswordRequestDtoValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return ResetPasswordRequestDto.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    ResetPasswordRequestDto resetPasswordRequestDto = (ResetPasswordRequestDto) target;

    if (resetPasswordRequestDto.getPassword().length() < 8 ||
        resetPasswordRequestDto.getPassword().length() > 20 ||
        !containsSpecialCharacter(resetPasswordRequestDto.getPassword())) {
      errors.rejectValue("password", "PASSWORD_FORM_ERROR");
    }
  }

  // 특수문자 포함 여부를 정규식으로 확인
  private boolean containsSpecialCharacter(String password) {
    return password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>/?]+.*");
  }
}
