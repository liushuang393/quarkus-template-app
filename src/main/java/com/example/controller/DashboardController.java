package com.example.controller;

import com.example.mapper.AuditLogMapper;
import com.example.mapper.UserMapper;
import com.example.model.AuditLog;
import com.example.service.MessageService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/** ダッシュボードAPI */
@Path("/api/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "ダッシュボード", description = "ダッシュボード関連のAPI")
public class DashboardController {

  @Inject UserMapper userMapper;

  @Inject AuditLogMapper auditLogMapper;

  @Inject MessageService messageService;

  @Context SecurityContext securityContext;

  @Context HttpHeaders headers;

  /** ダッシュボード統計情報取得 */
  @GET
  @Path("/stats")
  @RolesAllowed({"ADMIN", "USER", "SALES"})
  @Operation(summary = "ダッシュボード統計情報取得", description = "ユーザー数、ログイン数などの統計情報を取得します")
  @APIResponse(responseCode = "200", description = "取得成功")
  public Response getStatistics() {
    try {
      Map<String, Object> stats = new HashMap<>();

      // ユーザー統計
      long totalUsers = userMapper.count();
      long activeUsers = userMapper.countActive();

      stats.put("totalUsers", totalUsers);
      stats.put("activeUsers", activeUsers);
      stats.put("inactiveUsers", totalUsers - activeUsers);

      // 今日のログイン数（監査ログから取得）
      LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
      LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

      List<AuditLog> todayLogins = auditLogMapper.findByDateRange(startOfDay, endOfDay);
      long todayLoginCount =
          todayLogins.stream()
              .filter(
                  log ->
                      "USER_LOGIN".equals(log.getAction())
                          && AuditLog.Status.SUCCESS.equals(log.getStatus()))
              .count();

      stats.put("todayLogins", todayLoginCount);

      // システムステータス（簡易実装）
      stats.put("systemStatus", "online");

      // ロール別統計（管理者のみ）
      if (securityContext.isUserInRole("ADMIN")) {
        Map<String, Long> roleStats = new HashMap<>();
        roleStats.put("ADMIN", userMapper.countByRole(com.example.model.User.Role.ADMIN));
        roleStats.put("USER", userMapper.countByRole(com.example.model.User.Role.USER));
        roleStats.put("SALES", userMapper.countByRole(com.example.model.User.Role.SALES));
        stats.put("roleStats", roleStats);
      }

      return Response.ok(stats).build();

    } catch (Exception e) {
      String message = messageService.getMessage("error.internal.server.error", headers);
      return Response.status(500).entity(Map.of("error", message)).build();
    }
  }

  /** 最近のアクティビティ取得 */
  @GET
  @Path("/activity")
  @RolesAllowed({"ADMIN", "USER", "SALES"})
  @Operation(summary = "最近のアクティビティ取得", description = "最近のユーザーアクティビティを取得します")
  @APIResponse(responseCode = "200", description = "取得成功")
  public Response getRecentActivity(
      @QueryParam("limit") Integer limit, @QueryParam("offset") Integer offset) {

    try {
      int pageLimit = limit != null ? limit : 10;
      int pageOffset = offset != null ? offset : 0;

      List<AuditLog> activities;

      if (securityContext.isUserInRole("ADMIN")) {
        // 管理者は全てのアクティビティを表示
        activities = auditLogMapper.findAll(pageLimit, pageOffset);
      } else {
        // 一般ユーザーは自分のアクティビティのみ
        String username = securityContext.getUserPrincipal().getName();
        activities = auditLogMapper.findByUsername(username);

        // ページング適用
        int start = Math.min(pageOffset, activities.size());
        int end = Math.min(start + pageLimit, activities.size());
        activities = activities.subList(start, end);
      }

      return Response.ok(activities).build();

    } catch (Exception e) {
      String message = messageService.getMessage("error.internal.server.error", headers);
      return Response.status(500).entity(Map.of("error", message)).build();
    }
  }

  /** システムヘルス情報取得 */
  @GET
  @Path("/health")
  @RolesAllowed({"ADMIN"})
  @Operation(summary = "システムヘルス情報取得", description = "システムの健康状態を取得します（管理者のみ）")
  @APIResponse(responseCode = "200", description = "取得成功")
  public Response getSystemHealth() {
    try {
      Map<String, Object> health = new HashMap<>();

      // データベース接続チェック
      try {
        userMapper.count();
        health.put(
            "database",
            Map.of(
                "status", "UP",
                "message", "Database connection is healthy"));
      } catch (Exception e) {
        health.put(
            "database",
            Map.of("status", "DOWN", "message", "Database connection failed: " + e.getMessage()));
      }

      // メモリ使用量
      Runtime runtime = Runtime.getRuntime();
      long totalMemory = runtime.totalMemory();
      long freeMemory = runtime.freeMemory();
      long usedMemory = totalMemory - freeMemory;

      health.put(
          "memory",
          Map.of(
              "total", totalMemory,
              "used", usedMemory,
              "free", freeMemory,
              "usagePercent", (double) usedMemory / totalMemory * 100));

      // システム情報
      health.put(
          "system",
          Map.of(
              "javaVersion", System.getProperty("java.version"),
              "osName", System.getProperty("os.name"),
              "osVersion", System.getProperty("os.version"),
              "processors", Runtime.getRuntime().availableProcessors()));

      // 全体ステータス
      boolean isHealthy =
          health.entrySet().stream()
              .filter(entry -> entry.getValue() instanceof Map)
              .map(
                  entry -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) entry.getValue();
                    return map;
                  })
              .filter(map -> map.containsKey("status"))
              .allMatch(map -> "UP".equals(map.get("status")));

      health.put("status", isHealthy ? "UP" : "DOWN");
      health.put("timestamp", LocalDateTime.now());

      return Response.ok(health).build();

    } catch (Exception e) {
      String message = messageService.getMessage("error.internal.server.error", headers);
      return Response.status(500).entity(Map.of("error", message)).build();
    }
  }

  /** ユーザーアクティビティサマリー取得 */
  @GET
  @Path("/user-activity-summary")
  @RolesAllowed({"ADMIN", "SALES"})
  @Operation(summary = "ユーザーアクティビティサマリー取得", description = "ユーザーのアクティビティサマリーを取得します")
  @APIResponse(responseCode = "200", description = "取得成功")
  public Response getUserActivitySummary(@QueryParam("days") Integer days) {
    try {
      int periodDays = days != null ? days : 7; // デフォルト7日間

      LocalDateTime startDate = LocalDateTime.now().minusDays(periodDays);
      LocalDateTime endDate = LocalDateTime.now();

      List<AuditLog> activities = auditLogMapper.findByDateRange(startDate, endDate);

      // アクション別集計
      Map<String, Long> actionCounts = new HashMap<>();
      Map<String, Long> dailyCounts = new HashMap<>();
      Map<String, Long> userCounts = new HashMap<>();

      for (AuditLog activity : activities) {
        // アクション別
        actionCounts.merge(activity.getAction(), 1L, Long::sum);

        // 日別
        String date = activity.getCreatedAt().toLocalDate().toString();
        dailyCounts.merge(date, 1L, Long::sum);

        // ユーザー別
        if (activity.getUsername() != null) {
          userCounts.merge(activity.getUsername(), 1L, Long::sum);
        }
      }

      Map<String, Object> summary = new HashMap<>();
      summary.put(
          "period",
          Map.of(
              "startDate", startDate,
              "endDate", endDate,
              "days", periodDays));
      summary.put("totalActivities", activities.size());
      summary.put("actionCounts", actionCounts);
      summary.put("dailyCounts", dailyCounts);
      summary.put("topUsers", userCounts);

      return Response.ok(summary).build();

    } catch (Exception e) {
      String message = messageService.getMessage("error.internal.server.error", headers);
      return Response.status(500).entity(Map.of("error", message)).build();
    }
  }
}
