// Copyright (c) 2024 Quarkus Template Project
//
// Licensed under the MIT License.
// See LICENSE file in the project root for full license information.

package com.example.service;

import com.example.mapper.AuditLogMapper;
import com.example.model.AuditLog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

/** 監査ログサービス */
@ApplicationScoped
public class AuditLogService {

  private static final Logger LOG = Logger.getLogger(AuditLogService.class);

  @Inject AuditLogMapper auditLogMapper;

  @Transactional
  public void log(Long userId, String username, String action) {
    log(userId, username, action, null, null, null);
  }

  @Transactional
  public void log(
      Long userId, String username, String action, String resourceType, String resourceId) {
    log(userId, username, action, resourceType, resourceId, null);
  }

  @Transactional
  public void log(
      Long userId,
      String username,
      String action,
      String resourceType,
      String resourceId,
      String details) {
    try {
      AuditLog auditLog = new AuditLog(userId, username, action, resourceType, resourceId);
      auditLog.setDetails(details);
      auditLog.setRequestId((String) MDC.get("requestId"));
      auditLogMapper.insert(auditLog);

      LOG.infof(
          "監査ログ記録: user=%s, action=%s, resource=%s:%s", username, action, resourceType, resourceId);

    } catch (Exception e) {
      LOG.errorf(e, "監査ログの記録に失敗しました: user=%s, action=%s", username, action);
    }
  }

  @Transactional
  public void logSuccess(
      Long userId, String username, String action, String resourceType, String resourceId) {
    logWithStatus(
        userId, username, action, resourceType, resourceId, null, AuditLog.Status.SUCCESS, null);
  }

  @Transactional
  public void logFailure(
      Long userId,
      String username,
      String action,
      String resourceType,
      String resourceId,
      String errorMessage) {
    logWithStatus(
        userId,
        username,
        action,
        resourceType,
        resourceId,
        null,
        AuditLog.Status.FAILURE,
        errorMessage);
  }

  @Transactional
  public void logError(
      Long userId,
      String username,
      String action,
      String resourceType,
      String resourceId,
      String errorMessage) {
    logWithStatus(
        userId,
        username,
        action,
        resourceType,
        resourceId,
        null,
        AuditLog.Status.ERROR,
        errorMessage);
  }

  private void logWithStatus(
      Long userId,
      String username,
      String action,
      String resourceType,
      String resourceId,
      String details,
      AuditLog.Status status,
      String errorMessage) {
    try {
      AuditLog auditLog = new AuditLog(userId, username, action, resourceType, resourceId);
      auditLog.setDetails(details);
      auditLog.setStatus(status);
      auditLog.setErrorMessage(errorMessage);
      auditLog.setRequestId((String) MDC.get("requestId"));
      auditLogMapper.insert(auditLog);

      LOG.infof(
          "監査ログ記録: user=%s, action=%s, resource=%s:%s, status=%s",
          username, action, resourceType, resourceId, status);

    } catch (Exception e) {
      LOG.errorf(e, "監査ログの記録に失敗しました: user=%s, action=%s", username, action);
    }
  }
}
