// Copyright (c) 2024 Quarkus Template Project
//
// Licensed under the MIT License.
// See LICENSE file in the project root for full license information.

package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

  @NotBlank(message = "ユーザー名は必須です")
  @Size(max = 50, message = "ユーザー名は50文字以下で入力してください")
  public String username;

  @NotBlank(message = "パスワードは必須です")
  @Size(max = 100, message = "パスワードは100文字以下で入力してください")
  public String password;
}
