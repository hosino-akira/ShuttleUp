package com.shuttleup.backend.controller;

import com.shuttleup.backend.exception.BadRequestException;
import com.shuttleup.backend.exception.EmailAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * APIで共通して扱う例外をHTTPレスポンスへ変換する。
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthentication(AuthenticationException exception) {
        return ResponseEntity.status(exception instanceof DisabledException ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", exception.getMessage()));
    }

    /** リクエストの入力検証に失敗した場合は400を返す。 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse("入力内容を確認してください。");
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    /** 登録時にメールアドレスが重複している場合は409を返す。 */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEmail(EmailAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", exception.getMessage()));
    }

    /** 入力内容が業務ルールに反する場合は400を返す。 */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(
            BadRequestException exception) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", exception.getMessage()));
    }

    /**
     * 指定されたリソースが存在しない場合は404を返す。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
    }

    /**
     * 関連データが存在して削除できない場合は409を返す。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleConflict() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "関連するデータが存在するため削除できません。"));
    }

    /** 業務上許可されない操作の場合は409を返す。 */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(
            IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", exception.getMessage()));
    }
}
