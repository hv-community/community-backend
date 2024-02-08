package com.hv.community.backend.dto.member;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RefreshRequestDto {

  @NotBlank(message = "VALID:TOKEN_INVALID")
  private String refreshToken;
}
