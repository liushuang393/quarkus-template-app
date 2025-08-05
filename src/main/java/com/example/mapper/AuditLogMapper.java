package com.example.mapper;

import com.example.model.AuditLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.*;

/** 監査ログマッパー（MyBatis） */
@Mapper
public interface AuditLogMapper {

  /** 監査ログを挿入 */
  @Insert(
      "INSERT INTO audit_logs (user_id, username, action, resource_type, resource_id, details,"
          + " ip_address, user_agent, request_id, status, error_message, created_at) VALUES"
          + " (#{userId}, #{username}, #{action}, #{resourceType}, #{resourceId}, #{details},"
          + " #{ipAddress}, #{userAgent}, #{requestId}, #{status}, #{errorMessage}, #{createdAt})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insert(AuditLog auditLog);

  /** IDで監査ログを検索 */
  @Select(
      "SELECT id, user_id, username, action, resource_type, resource_id, "
          + "details, ip_address, user_agent, request_id, status, error_message, created_at "
          + "FROM audit_logs WHERE id = #{id}")
  @Results({
    @Result(property = "id", column = "id"),
    @Result(property = "userId", column = "user_id"),
    @Result(property = "username", column = "username"),
    @Result(property = "action", column = "action"),
    @Result(property = "resourceType", column = "resource_type"),
    @Result(property = "resourceId", column = "resource_id"),
    @Result(property = "details", column = "details"),
    @Result(property = "ipAddress", column = "ip_address"),
    @Result(property = "userAgent", column = "user_agent"),
    @Result(property = "requestId", column = "request_id"),
    @Result(property = "status", column = "status"),
    @Result(property = "errorMessage", column = "error_message"),
    @Result(property = "createdAt", column = "created_at")
  })
  Optional<AuditLog> findById(Long id);

  /** ユーザーIDで監査ログを検索 */
  @Select(
      "SELECT id, user_id, username, action, resource_type, resource_id, "
          + "details, ip_address, user_agent, request_id, status, error_message, created_at "
          + "FROM audit_logs WHERE user_id = #{userId} ORDER BY created_at DESC")
  @Results({
    @Result(property = "id", column = "id"),
    @Result(property = "userId", column = "user_id"),
    @Result(property = "username", column = "username"),
    @Result(property = "action", column = "action"),
    @Result(property = "resourceType", column = "resource_type"),
    @Result(property = "resourceId", column = "resource_id"),
    @Result(property = "details", column = "details"),
    @Result(property = "ipAddress", column = "ip_address"),
    @Result(property = "userAgent", column = "user_agent"),
    @Result(property = "requestId", column = "request_id"),
    @Result(property = "status", column = "status"),
    @Result(property = "errorMessage", column = "error_message"),
    @Result(property = "createdAt", column = "created_at")
  })
  List<AuditLog> findByUserId(Long userId);

  /** ユーザー名で監査ログを検索 */
  @Select(
      "SELECT id, user_id, username, action, resource_type, resource_id, "
          + "details, ip_address, user_agent, request_id, status, error_message, created_at "
          + "FROM audit_logs WHERE username = #{username} ORDER BY created_at DESC")
  @Results({
    @Result(property = "id", column = "id"),
    @Result(property = "userId", column = "user_id"),
    @Result(property = "username", column = "username"),
    @Result(property = "action", column = "action"),
    @Result(property = "resourceType", column = "resource_type"),
    @Result(property = "resourceId", column = "resource_id"),
    @Result(property = "details", column = "details"),
    @Result(property = "ipAddress", column = "ip_address"),
    @Result(property = "userAgent", column = "user_agent"),
    @Result(property = "requestId", column = "request_id"),
    @Result(property = "status", column = "status"),
    @Result(property = "errorMessage", column = "error_message"),
    @Result(property = "createdAt", column = "created_at")
  })
  List<AuditLog> findByUsername(String username);

  /** アクションで監査ログを検索 */
  @Select(
      "SELECT id, user_id, username, action, resource_type, resource_id, "
          + "details, ip_address, user_agent, request_id, status, error_message, created_at "
          + "FROM audit_logs WHERE action = #{action} ORDER BY created_at DESC")
  @Results({
    @Result(property = "id", column = "id"),
    @Result(property = "userId", column = "user_id"),
    @Result(property = "username", column = "username"),
    @Result(property = "action", column = "action"),
    @Result(property = "resourceType", column = "resource_type"),
    @Result(property = "resourceId", column = "resource_id"),
    @Result(property = "details", column = "details"),
    @Result(property = "ipAddress", column = "ip_address"),
    @Result(property = "userAgent", column = "user_agent"),
    @Result(property = "requestId", column = "request_id"),
    @Result(property = "status", column = "status"),
    @Result(property = "errorMessage", column = "error_message"),
    @Result(property = "createdAt", column = "created_at")
  })
  List<AuditLog> findByAction(String action);

  /** 期間で監査ログを検索 */
  @Select(
      "SELECT id, user_id, username, action, resource_type, resource_id, "
          + "details, ip_address, user_agent, request_id, status, error_message, created_at "
          + "FROM audit_logs WHERE created_at BETWEEN #{startDate} AND #{endDate} "
          + "ORDER BY created_at DESC")
  @Results({
    @Result(property = "id", column = "id"),
    @Result(property = "userId", column = "user_id"),
    @Result(property = "username", column = "username"),
    @Result(property = "action", column = "action"),
    @Result(property = "resourceType", column = "resource_type"),
    @Result(property = "resourceId", column = "resource_id"),
    @Result(property = "details", column = "details"),
    @Result(property = "ipAddress", column = "ip_address"),
    @Result(property = "userAgent", column = "user_agent"),
    @Result(property = "requestId", column = "request_id"),
    @Result(property = "status", column = "status"),
    @Result(property = "errorMessage", column = "error_message"),
    @Result(property = "createdAt", column = "created_at")
  })
  List<AuditLog> findByDateRange(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  /** 全監査ログを取得（ページング対応） */
  @Select(
      "SELECT id, user_id, username, action, resource_type, resource_id, "
          + "details, ip_address, user_agent, request_id, status, error_message, created_at "
          + "FROM audit_logs ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
  @Results({
    @Result(property = "id", column = "id"),
    @Result(property = "userId", column = "user_id"),
    @Result(property = "username", column = "username"),
    @Result(property = "action", column = "action"),
    @Result(property = "resourceType", column = "resource_type"),
    @Result(property = "resourceId", column = "resource_id"),
    @Result(property = "details", column = "details"),
    @Result(property = "ipAddress", column = "ip_address"),
    @Result(property = "userAgent", column = "user_agent"),
    @Result(property = "requestId", column = "request_id"),
    @Result(property = "status", column = "status"),
    @Result(property = "errorMessage", column = "error_message"),
    @Result(property = "createdAt", column = "created_at")
  })
  List<AuditLog> findAll(@Param("limit") int limit, @Param("offset") int offset);

  /** 監査ログ数を取得 */
  @Select("SELECT COUNT(*) FROM audit_logs")
  long count();

  /** 古い監査ログを削除 */
  @Delete("DELETE FROM audit_logs WHERE created_at < #{cutoffDate}")
  int deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}
