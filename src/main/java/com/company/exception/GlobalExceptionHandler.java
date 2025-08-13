package com.company.exception;

import com.company.exception.model.ErrorResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(DataNotFoundException.class)
    public ErrorResponse handle(DataNotFoundException ex, WebRequest request) {
        log.trace("Resource not found {}", ex.getErrorMessage(), ex);
        return handleErrorResponse(request, ex.getErrorCode(), ex.getErrorMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AlreadyExistException.class)
    public ErrorResponse handle(AlreadyExistException ex, WebRequest request) {
        log.trace("Resource already exists {}", ex.getErrorMessage(), ex);
        return handleErrorResponse(request, ex.getErrorCode(), ex.getErrorMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(CommonException.class)
    public ErrorResponse handle(CommonException ex, WebRequest request) {
        log.trace("Internal server error {}", ex.getErrorMessage(), ex);
        return handleErrorResponse(request, ex.getErrorCode(), ex.getErrorMessage());
    }

    private ErrorResponse handleErrorResponse(WebRequest request, String errorCode, String errorMessage) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .error(errorCode)
                .message(errorMessage)
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
    }

}


