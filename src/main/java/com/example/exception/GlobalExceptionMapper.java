// Copyright (c) 2024 Quarkus Template Project
//
// Licensed under the MIT License.
// See LICENSE file in the project root for full license information.

package com.example.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import com.example.dto.ErrorResponse;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** グローバル例外ハンドラー */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

  private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

  @Context UriInfo uriInfo;

  @Context HttpHeaders headers;

  @Inject com.example.service.MessageService messageService;

  @Inject com.example.service.AuditLogService auditLogService;

  @Override
  public Response toResponse(Exception exception) {
    String path = uriInfo != null ? uriInfo.getPath() : "";


    // WebApplicationException（404/401/403 など既定のHTTPレスポンス）は素通しする
    if (exception instanceof jakarta.ws.rs.WebApplicationException) {
      return ((jakarta.ws.rs.WebApplicationException) exception).getResponse();
    }

    // バリデーション例外
    if (exception instanceof ConstraintViolationException) {
      return handleConstraintViolationException((ConstraintViolationException) exception, path);
    }

    // ビジネス例外
    if (exception instanceof BusinessException) {
      return handleBusinessException((BusinessException) exception, path);
    }

    // その他の例外
    LOG.error("予期しないエラーが発生しました", exception);
    String message = messageService.getMessage("error.internal.server.error", headers);
    ErrorResponse errorResponse = new ErrorResponse("INTERNAL_SERVER_ERROR", message, path);
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
  }

  private Response handleConstraintViolationException(
      ConstraintViolationException exception, String path) {
    List<ErrorResponse.FieldError> fieldErrors =
        exception.getConstraintViolations().stream()
            .map(this::toFieldError)
            .collect(Collectors.toList());

    String message = messageService.getMessage("error.validation.error", headers);
    ErrorResponse errorResponse = new ErrorResponse("VALIDATION_ERROR", message, path, fieldErrors);

    LOG.warn("バリデーションエラー: " + exception.getMessage());
    return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
  }

  private Response handleBusinessException(BusinessException exception, String path) {
    ErrorResponse errorResponse =
        new ErrorResponse(exception.getErrorCode(), exception.getMessage(), path);

    LOG.warn("Business error: " + exception.getMessage());

    // 审计日志记录（失败）- 从路径推断操作类型
    String action = inferActionFromPath(path);
    if (action != null) {
      auditLogService.logFailure(null, "unknown", action, "User", null, exception.getMessage());
    }

    return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
  }

  private ErrorResponse.FieldError toFieldError(ConstraintViolation<?> violation) {
    String fieldName = violation.getPropertyPath().toString();
    return new ErrorResponse.FieldError(
        fieldName, violation.getMessage(), violation.getInvalidValue());
  }

  /**
   * 从请求路径推断操作类型
   */
  private String inferActionFromPath(String path) {
    if (path == null) return null;

    if (path.contains("/auth/register")) {
      return "USER_REGISTER";
    } else if (path.contains("/auth/login")) {
      return "USER_LOGIN";
    } else if (path.contains("/user")) {
      return "USER_OPERATION";
    }

    return null;
  }
}
