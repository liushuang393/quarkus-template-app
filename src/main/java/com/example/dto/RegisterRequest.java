// Copyright (c) 2024 Quarkus Template Project
//
// Licensed under the MIT License.
// See LICENSE file in the project root for full license information.

package com.example.dto;

import com.example.model.User;
import jakarta.validation.constraints.*;

public class RegisterRequest {

  @NotBlank(message = "ユーザー名は必須です")
  @Size(min = 3, max = 50, message = "ユーザー名は3文字以上50文字以下で入力してください")
  @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "ユーザー名は英数字とアンダースコアのみ使用可能です")
  public String username;

  @NotBlank(message = "パスワードは必須です")
  @Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以下で入力してください")
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "パスワードは大文字、小文字、数字を含む必要があります")
  public String password;

  @NotBlank(message = "メールアドレスは必須です")
  @Email(message = "有効なメールアドレスを入力してください")
  @Size(max = 100, message = "メールアドレスは100文字以下で入力してください")
  public String email;

  @NotNull(message = "ロールは必須です")
  public User.Role role = User.Role.USER;
}
