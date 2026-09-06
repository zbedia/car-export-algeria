package com.carexport.exception;

import com.carexport.shipping.RouteNotSupportedException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidSearchCriteriaException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCriteria(
            InvalidSearchCriteriaException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(), ErrorMessages.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(RouteNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleRouteNotSupported(
            RouteNotSupportedException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(), ErrorMessages.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format(ErrorMessages.INVALID_PARAMETER, ex.getName());
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(), ErrorMessages.BAD_REQUEST, message, request.getRequestURI());
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Raised by JPA optimistic locking (@Version) when a write collides with
     * another transaction that already updated the same row. The client should
     * reload the latest state and retry.
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingConflict(
            OptimisticLockingFailureException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(), ErrorMessages.CONFLICT,
            ErrorMessages.OPTIMISTIC_LOCKING_CONFLICT, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Caught when two concurrent writes violate a constraint such as the
     * unique external_url — the client can safely retry the request.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(), ErrorMessages.CONFLICT,
            ErrorMessages.DATA_INTEGRITY_VIOLATION, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error(ErrorMessages.UNHANDLED_EXCEPTION, request.getMethod(), request.getRequestURI(), ex);
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorMessages.INTERNAL_SERVER_ERROR,
            ErrorMessages.UNEXPECTED_ERROR, request.getRequestURI());
        return ResponseEntity.internalServerError().body(error);
    }
}
