package com.hv.community.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(Include.NON_NULL)
public class ResponseDto {

  private String status;
  private String message;
  private Map<String, String> data;
  private Map<String, String> errors;
  private String detail;
}
