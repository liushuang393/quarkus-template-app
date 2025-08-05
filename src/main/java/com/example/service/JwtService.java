// Copyright (c) 2024 Quarkus Template Project
//
// Licensed under the MIT License.
// See LICENSE file in the project root for full license information.

package com.example.service;

import com.example.model.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class JwtService {

  @ConfigProperty(name = "mp.jwt.verify.issuer")
  String issuer;

  public String generateToken(User user) {
    return Jwt.issuer(issuer)
        .upn(user.getUsername())
        .groups(user.getRole().name())
        .claim("userId", user.getId())
        .claim("email", user.getEmail())
        .expiresIn(Duration.ofHours(24))
        .sign();
  }
}
