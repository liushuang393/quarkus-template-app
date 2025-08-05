// Copyright (c) 2024 Quarkus Template Project
//
// Licensed under the MIT License.
// See LICENSE file in the project root for full license information.

package com.example.exception;

/** ビジネスロジック例外 */
public class BusinessException extends RuntimeException {

  private final String errorCode;

  public BusinessException(String message) {
    super(message);
    this.errorCode = "BUSINESS_ERROR";
  }

  public BusinessException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public BusinessException(String message, Throwable cause) {
    super(message, cause);
    this.errorCode = "BUSINESS_ERROR";
  }

  public BusinessException(String errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
