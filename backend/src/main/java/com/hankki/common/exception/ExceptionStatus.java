package com.hankki.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ExceptionStatus {

	// DIET
	INVALID_MEAL_TYPE(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 식사 종류입니다."),
	INVALID_MAJOR_CATEGORY(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 대분류입니다."),
	INVALID_DIET_INPUT(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 식사 입력입니다."),
	NOT_FOUND_DIET(HttpStatus.NOT_FOUND, 404, "식사 정보가 존재하지 않습니다."),
	NOT_FOUND_DIET_MEAL_ITEM(HttpStatus.NOT_FOUND, 404, "식사에 대응하는 음식 정보를 찾지 못했습니다."),
	NOT_FOUND_MEAL_ITEM(HttpStatus.NOT_FOUND, 404, "음식 정보가 존재하지 않습니다."),

	// DB
	FAIL_TO_CREATE_ENTITY(HttpStatus.BAD_REQUEST, 400, "엔티티 생성에 실패했습니다."),
	FAIL_TO_DELETE_ENTITY(HttpStatus.BAD_REQUEST, 400, "엔티티 삭제에 실패했습니다."),
	FAIL_TO_UPDATE_ENTITY(HttpStatus.BAD_REQUEST, 400, "엔티티 수정에 실패했습니다."),

	// USER
	NOT_FOUND_USER(HttpStatus.NOT_FOUND, 404, "사용자를 찾지 못했습니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, 403, "허가된 사용자가 아닙니다."),

	EMAIL_DUPLICATION(HttpStatus.CONFLICT, 409, "이미 사용 중인 이메일입니다."),
	NICKNAME_DUPLICATION(HttpStatus.CONFLICT, 409, "이미 사용 중인 닉네임입니다."),
	NOT_FOUND_USER_HEALTH(HttpStatus.NOT_FOUND, 404, "해당 사용자 ID의 건강정보가 없습니다."),

	// FOOD
	NOT_FOUND_FOOD(HttpStatus.NOT_FOUND, 404, "음식을 찾지 못했습니다."),
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 값을 입력했습니다"),

	// RECOMMEND
	RECOMMEND_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, 429, "오늘 추천 횟수를 모두 사용했습니다."),
	EMPTY_DIET_REQUEST(HttpStatus.BAD_REQUEST, 400, "최근 3일 간 식단 기록이 비었습니다."),
	NOT_FOUND_VECTOR(HttpStatus.NOT_FOUND, 404, "벡터가 존재하지 않습니다."),
	INVALID_VECTOR_DIMENSION(HttpStatus.BAD_REQUEST, 400, "벡터 차원이 올바르지 않습니다."),

	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "내부 서버 오류 발생");

	private final HttpStatus httpStatus;
	private final int errorCode;
	private final String message;

	ExceptionStatus(HttpStatus httpStatus, int errorCode, String message) {
		this.httpStatus = httpStatus;
		this.errorCode = errorCode;
		this.message = message;
	}

}
