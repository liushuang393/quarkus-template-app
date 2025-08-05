package com.example.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * エラーレスポンス
 */
public class ErrorResponse {
    
    public String errorCode;
    public String message;
    public LocalDateTime timestamp;
    public String path;
    public List<FieldError> fieldErrors;
    
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(String errorCode, String message) {
        this();
        this.errorCode = errorCode;
        this.message = message;
    }
    
    public ErrorResponse(String errorCode, String message, String path) {
        this(errorCode, message);
        this.path = path;
    }
    
    public ErrorResponse(String errorCode, String message, String path, List<FieldError> fieldErrors) {
        this(errorCode, message, path);
        this.fieldErrors = fieldErrors;
    }
    
    public static class FieldError {
        public String field;
        public String message;
        public Object rejectedValue;
        
        public FieldError() {}
        
        public FieldError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }
    }
}
