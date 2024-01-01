package com.hv.community.backend.dto.member;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class SendResetEmailRequestDtoValidator implements Validator {

  private static final String EMAIL_REG_EXP = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
      + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

  private final Pattern pattern;

  public SendResetEmailRequestDtoValidator() {
    pattern = Pattern.compile(EMAIL_REG_EXP);
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return SendResetPasswordEmailRequestDto.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    SendResetPasswordEmailRequestDto sendResetPasswordEmailRequestDto = (SendResetPasswordEmailRequestDto) target;

    if (sendResetPasswordEmailRequestDto.getEmail() == null
        || sendResetPasswordEmailRequestDto.getEmail().trim()
        .isEmpty()) {
      errors.rejectValue("email", "EMAIL_EMPTY");
    } else {
      Matcher matcher = pattern.matcher(sendResetPasswordEmailRequestDto.getEmail());
      if (!matcher.matches()) {
        errors.rejectValue("email", "EMAIL_FORM_ERROR");
      }
    }
  }
}
