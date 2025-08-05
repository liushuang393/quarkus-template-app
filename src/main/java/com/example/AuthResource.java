// Copyright (c) 2024 Quarkus Template Project
//
// Licensed under the MIT License.
// See LICENSE file in the project root for full license information.

package com.example;

import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.model.User;
import com.example.service.JwtService;
import com.example.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "認証", description = "ユーザー認証関連のAPI")
public class AuthResource {

  @Inject UserService userService;

  @Inject JwtService jwtService;

  @Inject com.example.service.AuditLogService auditLogService;

  @Inject com.example.service.MessageService messageService;

  @Context HttpHeaders headers;

  @POST
  @Path("/register")
  @Operation(summary = "ユーザー登録", description = "新しいユーザーを登録します")
  @APIResponse(
      responseCode = "200",
      description = "登録成功",
      content = @Content(mediaType = "application/json"))
  @APIResponse(
      responseCode = "400",
      description = "バリデーションエラー",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = com.example.dto.ErrorResponse.class)))
  public Response register(@jakarta.validation.Valid RegisterRequest request) {
    User user = userService.register(request, headers);

    // 監査ログ記録
    auditLogService.logSuccess(
        user.getId(), user.getUsername(), "USER_REGISTER", "User", user.getId().toString());

    String message = messageService.getMessage("auth.register.success", headers);
    return Response.ok(Map.of("message", message, "userId", user.getId())).build();
  }

  @POST
  @Path("/login")
  @Operation(summary = "ログイン", description = "ユーザー認証を行い、JWTトークンを発行します")
  @APIResponse(
      responseCode = "200",
      description = "ログイン成功",
      content = @Content(mediaType = "application/json"))
  @APIResponse(
      responseCode = "401",
      description = "認証失敗",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = com.example.dto.ErrorResponse.class)))
  public Response login(@jakarta.validation.Valid LoginRequest request) {
    return userService
        .authenticate(request.username, request.password)
        .map(
            user -> {
              String token = jwtService.generateToken(user);

              // 監査ログ記録（成功）
              auditLogService.logSuccess(
                  user.getId(), user.getUsername(), "USER_LOGIN", "User", user.getId().toString());

              return Response.ok(
                      Map.of(
                          "token",
                          token,
                          "user",
                          Map.of(
                              "id", user.getId(),
                              "username", user.getUsername(),
                              "role", user.getRole())))
                  .build();
            })
        .orElseGet(
            () -> {
              // 監査ログ記録（失敗）
              String errorMessage =
                  messageService.getMessage("error.authentication.failed", headers);
              auditLogService.logFailure(
                  null, request.username, "USER_LOGIN", "User", null, errorMessage);

              com.example.dto.ErrorResponse errorResponse =
                  new com.example.dto.ErrorResponse(
                      "AUTHENTICATION_FAILED", errorMessage, "/auth/login");

              return Response.status(401).entity(errorResponse).build();
            });
  }
}
