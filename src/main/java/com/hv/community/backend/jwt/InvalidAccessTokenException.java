package com.hv.community.backend.jwt;

public class InvalidAccessTokenException extends RuntimeException {

  public InvalidAccessTokenException() {
  }

  public InvalidAccessTokenException(String message) {
    super(message);
  }
}
