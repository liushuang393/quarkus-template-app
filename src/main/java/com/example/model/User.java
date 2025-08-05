package com.example.model;

import java.time.LocalDateTime;

/** ユーザーモデル（MyBatis用POJO） */
public class User {

  private Long id;
  private String username;
  private String password;
  private String email;
  private Role role = Role.USER;
  private LocalDateTime createdAt = LocalDateTime.now();
  private boolean isActive = true;

  public enum Role {
    ADMIN,
    USER,
    SALES
  }

  // デフォルトコンストラクタ
  public User() {}

  // コンストラクタ
  public User(String username, String password, String email, Role role) {
    this.username = username;
    this.password = password;
    this.email = email;
    this.role = role;
  }

  // Getter/Setter
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  @Override
  public String toString() {
    return "User{"
        + "id="
        + id
        + ", username='"
        + username
        + '\''
        + ", email='"
        + email
        + '\''
        + ", role="
        + role
        + ", createdAt="
        + createdAt
        + ", isActive="
        + isActive
        + '}';
  }
}
