package com.hankki.domain.diet.constant;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum MealType {
	MORNING (0, "아침"),
	LUNCH (1, "점심"),
	DINNER (2, "저녁"),
	SNACK (3, "간식"),
	TODAY(9, "오늘");

	private final int code;
	private final String description;
	
	MealType(int code, String description) {
		this.code = code;
		this.description = description;
	}

	/**
	 * 코드로부터 해당 enum 을 반환.
	 * 없으면 IllegalArgumentException 발생.
	 */
	public static MealType fromCode(int code) {
		return Arrays.stream(values())
				.filter(mt -> mt.code == code)
				.findFirst()
				.orElseThrow(() ->
						new IllegalArgumentException("Invalid MealType code: " + code)
				);
	}
}
