package com.hankki.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ExceptionStatus {
	
	// DIET
	INVALID_MEAL_TYPE(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 식사 종류입니다."),
	INVALID_MAJOR_CATEGORY(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 대분류입니다.");
	
	
	private final HttpStatus httpStatus;
	private final int errorCode;
	private final String message;
	
	ExceptionStatus (HttpStatus httpStatus, int errorCode, String message) {
		this.httpStatus = httpStatus;
		this.errorCode = errorCode;
		this.message = message;
	}
	
}
