package com.example.tasktracker.exception;

import java.time.Instant;
import java.util.List;

public class ErrorResponse {

	private int statusCode;
	private String message;
	private List<FieldViolation> errors;
	private String timestamp;
	private String path;

	public ErrorResponse() {
	}

	public ErrorResponse(int statusCode, String message, String path, List<FieldViolation> errors) {
		this.statusCode = statusCode;
		this.message = message;
		this.path = path;
		this.errors = errors;
		this.timestamp = Instant.now().toString();
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<FieldViolation> getErrors() {
		return errors;
	}

	public void setErrors(List<FieldViolation> errors) {
		this.errors = errors;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public static class FieldViolation {

		private String field;
		private String message;

		public FieldViolation() {
		}

		public FieldViolation(String field, String message) {
			this.field = field;
			this.message = message;
		}

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
}
