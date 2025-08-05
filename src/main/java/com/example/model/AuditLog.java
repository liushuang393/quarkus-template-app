// Copyright (c) 2024 Quarkus Template Project
//
// Licensed under the MIT License.
// See LICENSE file in the project root for full license information.

package com.example.model;

import java.time.LocalDateTime;

/** 監査ログモデル（MyBatis用POJO） */
public class AuditLog {

  private Long id;
  private Long userId;
  private String username;
  private String action;
  private String resourceType;
  private String resourceId;
  private String details;
  private String ipAddress;
  private String userAgent;
  private String requestId;
  private Status status = Status.SUCCESS;
  private String errorMessage;
  private LocalDateTime createdAt = LocalDateTime.now();

  public enum Status {
    SUCCESS,
    FAILURE,
    ERROR
  }

  // デフォルトコンストラクタ
  public AuditLog() {}

  // コンストラクタ
  public AuditLog(Long userId, String username, String action) {
    this.userId = userId;
    this.username = username;
    this.action = action;
  }

  public AuditLog(
      Long userId, String username, String action, String resourceType, String resourceId) {
    this(userId, username, action);
    this.resourceType = resourceType;
    this.resourceId = resourceId;
  }

  // Getter/Setter
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public String toString() {
    return "AuditLog{"
        + "id="
        + id
        + ", userId="
        + userId
        + ", username='"
        + username
        + '\''
        + ", action='"
        + action
        + '\''
        + ", resourceType='"
        + resourceType
        + '\''
        + ", resourceId='"
        + resourceId
        + '\''
        + ", status="
        + status
        + ", createdAt="
        + createdAt
        + '}';
  }
}
