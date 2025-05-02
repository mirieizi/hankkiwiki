package com.hankki.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

public enum ExceptionStatus {
	// DIET
	INVALID_DIET_MEAL_STATUS(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 식사 정보입니다.");
	
	
	
	private final HttpStatus httpStatus;
	private final int errorCode;
	private final String message;
	
	ExceptionStatus (HttpStatus httpStatus, int errorCode, String message) {
		this.httpStatus = httpStatus;
		this.errorCode = errorCode;
		this.message = message;
	}

	public HttpStatus getStatus() {
		return httpStatus;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getMessage() {
		return message;
	}
	
}
