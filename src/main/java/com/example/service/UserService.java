// Copyright (c) 2024 Quarkus Template Project
//
// Licensed under the MIT License.
// See LICENSE file in the project root for full license information.

package com.example.service;

import java.util.Optional;

import org.jboss.logging.Logger;

import com.example.dto.RegisterRequest;
import com.example.mapper.UserMapper;
import com.example.model.User;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.HttpHeaders;

@ApplicationScoped
public class UserService {

  private static final Logger LOG = Logger.getLogger(UserService.class);

  @Inject MessageService messageService;

  @Inject UserMapper userMapper;

  @Transactional
  public User register(RegisterRequest request, HttpHeaders headers) {
    LOG.infof("User registration started: username=%s, email=%s", request.username, request.email);

    // 重複チェック
    Optional<User> existingUser = userMapper.findByUsername(request.username);
    if (existingUser.isPresent()) {
      LOG.warnf("User registration failed: username already exists - username=%s", request.username);
      String message = messageService.getMessage("error.user.already.exists", headers);
      throw new com.example.exception.BusinessException("USER_ALREADY_EXISTS", message);
    }

    // ユーザー作成
    User user = new User();
    user.setUsername(request.username);
    user.setPassword(BcryptUtil.bcryptHash(request.password));
    user.setEmail(request.email);
    user.setRole(request.role);

    LOG.debugf("Starting database insertion: username=%s", request.username);
    userMapper.insert(user);

    LOG.infof("User registration successful: userId=%d, username=%s", user.getId(), user.getUsername());
    return user;
  }

  @Transactional
  public User register(RegisterRequest request) {
    return register(request, null);
  }

  public Optional<User> authenticate(String username, String password) {
    Optional<User> userOpt = userMapper.findActiveByUsername(username);
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      if (BcryptUtil.matches(password, user.getPassword())) {
        return Optional.of(user);
      }
    }
    return Optional.empty();
  }
}
