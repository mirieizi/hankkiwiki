package com.hankki.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ExceptionStatus {
	
	// DIET
	INVALID_MEAL_TYPE(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 식사 종류입니다."),
	INVALID_MAJOR_CATEGORY(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 대분류입니다."),
	NOT_FOUND_DIET(HttpStatus.NOT_FOUND, 404, "식사 정보가 존재하지 않습니다."),
	NOT_FOUND_DIET_MEAL_ITEM(HttpStatus.NOT_FOUND, 404, "식사에 대응하는 음식 정보를 찾지 못했습니다."),
	NOT_FOUND_MEAL_ITEM(HttpStatus.NOT_FOUND, 404, "음식 정보가 존재하지 않습니다."),

	// DB
	FAIL_TO_CREATE_ENTITY(HttpStatus.BAD_REQUEST, 400, "엔티티 생성에 실패했습니다."),
	FAIL_TO_DELETE_ENTITY(HttpStatus.BAD_REQUEST, 400, "엔티티 삭제에 실패했습니다."),
	FAIL_TO_UPDATE_ENTITY(HttpStatus.BAD_REQUEST, 400, "엔티티 수정에 실패했습니다."),

	// USER
	NOT_FOUND_USER(HttpStatus.NOT_FOUND, 404, "사용자를 찾지 못했습니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, 403, "허가된 사용자가 아닙니다.");

	private final HttpStatus httpStatus;
	private final int errorCode;
	private final String message;
	
	ExceptionStatus (HttpStatus httpStatus, int errorCode, String message) {
		this.httpStatus = httpStatus;
		this.errorCode = errorCode;
		this.message = message;
	}
	
}
