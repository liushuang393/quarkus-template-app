// Copyright (c) 2024 Quarkus Template Project
//
// Licensed under the MIT License.
// See LICENSE file in the project root for full license information.

package com.example.interceptor;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.UUID;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

/** リクエスト/レスポンスログインターセプター */
@Provider
public class LoggingInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

  private static final Logger LOG = Logger.getLogger(LoggingInterceptor.class);
  private static final String REQUEST_ID_HEADER = "X-Request-ID";
  private static final String REQUEST_ID_KEY = "requestId";
  private static final String START_TIME_KEY = "startTime";

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    // リクエストIDの生成または取得
    String requestId = requestContext.getHeaderString(REQUEST_ID_HEADER);
    if (requestId == null || requestId.isEmpty()) {
      requestId = UUID.randomUUID().toString();
    }

    // MDCにリクエストIDを設定
    MDC.put(REQUEST_ID_KEY, requestId);
    MDC.put(START_TIME_KEY, String.valueOf(System.currentTimeMillis()));

    // リクエストログ
    LOG.infof(
        "HTTP Request: %s %s from %s",
        requestContext.getMethod(),
        requestContext.getUriInfo().getPath(),
        getClientIp(requestContext));

    // レスポンスヘッダーにリクエストIDを設定
    requestContext.setProperty(REQUEST_ID_KEY, requestId);
  }

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    try {
      String requestId = (String) requestContext.getProperty(REQUEST_ID_KEY);
      String startTimeStr = (String) MDC.get(START_TIME_KEY);

      // レスポンスヘッダーにリクエストIDを追加
      if (requestId != null) {
        responseContext.getHeaders().add(REQUEST_ID_HEADER, requestId);
      }

      // 処理時間の計算
      long duration = 0;
      if (startTimeStr != null) {
        long startTime = Long.parseLong(startTimeStr);
        duration = System.currentTimeMillis() - startTime;
      }

      // レスポンスログ
      LOG.infof(
          "HTTP Response: %s %s -> %d (%dms)",
          requestContext.getMethod(),
          requestContext.getUriInfo().getPath(),
          responseContext.getStatus(),
          duration);

    } finally {
      // MDCをクリア
      MDC.clear();
    }
  }

  private String getClientIp(ContainerRequestContext requestContext) {
    String xForwardedFor = requestContext.getHeaderString("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }

    String xRealIp = requestContext.getHeaderString("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }

    return "unknown";
  }
}
