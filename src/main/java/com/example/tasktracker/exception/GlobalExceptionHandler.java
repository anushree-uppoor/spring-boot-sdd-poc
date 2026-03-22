package com.example.tasktracker.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(
			MethodArgumentNotValidException ex,
			HttpServletRequest request) {

		List<ErrorResponse.FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> new ErrorResponse.FieldViolation(fe.getField(), fe.getDefaultMessage()))
				.collect(Collectors.toList());

		ErrorResponse body = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"Validation failed",
				request.getRequestURI(),
				violations);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}
}
