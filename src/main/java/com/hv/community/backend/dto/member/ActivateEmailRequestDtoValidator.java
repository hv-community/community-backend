package com.hv.community.backend.dto.member;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ActivateEmailRequestDtoValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return ActivateEmailRequestDto.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    ActivateEmailRequestDto activateEmailRequestDto = (ActivateEmailRequestDto) target;

    // name 검증
    if (activateEmailRequestDto.getName() == null || activateEmailRequestDto.getName().trim()
        .isEmpty()) {
      errors.rejectValue("name", "NAME_EMPTY");
    } else if (activateEmailRequestDto.getName().length() < 4
        || activateEmailRequestDto.getName().length() > 20) {
      errors.rejectValue("name", "NAME_FORM_ERROR");
    }

    // 비밀번호 검증
    if (activateEmailRequestDto.getPassword().length() < 8 ||
        activateEmailRequestDto.getPassword().length() > 20 ||
        !containsSpecialCharacter(activateEmailRequestDto.getPassword())) {
      errors.rejectValue("password", "PASSWORD_FORM_ERROR");
    }
  }

  // 특수문자 포함 여부를 정규식으로 확인
  private boolean containsSpecialCharacter(String password) {
    return password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>/?]+.*");
  }
}
