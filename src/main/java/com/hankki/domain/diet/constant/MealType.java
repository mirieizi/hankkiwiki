package com.hankki.domain.diet.constant;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;

import lombok.Getter;

@Getter
public enum MealType {
	MORNING (0, "아침 식사"), 
	LUNCH (1, "점심 식사"), 
	DINNER (2, "저녁 식사"), 
	SNACK (3, "간식");
	
	private final int code;
	private final String description;
	
	MealType(int code, String description) {
		this.code = code;
		this.description = description;
	}
	
	public static MealType fromcode(int code) {
		for (MealType meal : values()) {
			if (meal.code == code) {
				return meal;
			}
		}
		throw new HankkiWikiException(ExceptionStatus.INVALID_MEAL_TYPE);
	}
	
}
