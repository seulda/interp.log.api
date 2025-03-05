package net.devgrr.interp.log.api.config.exception;

import lombok.Getter;

@Getter
public class ExtendRuntimeException extends RuntimeException {

  private final ErrorCode errorCode;

  public ExtendRuntimeException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }
}
