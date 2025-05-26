package com.hankki.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HankkiWikiException.class)
    public ResponseEntity<ErrorResponse> handleHankkiWikiException(HankkiWikiException e) {
        ExceptionStatus status = e.getInfo();
        log.warn("[예외 발생] {} - {}", status.name(), status.getMessage());
        return ResponseEntity
                .status((e.getInfo().getHttpStatus()))
                .body(new ErrorResponse(
                        status.getErrorCode(),
                        status.getMessage()
                ));
    }

    @Getter
    @AllArgsConstructor
    public static class ErrorResponse {
        private int code;
        private String message;
    }
}
