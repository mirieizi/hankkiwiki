package com.hankki.domain.diet.dto;

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
		throw new IllegalArgumentException("[UserDietMeal]유효하지 않은 식사 코드: " + code);
	}

	public int getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}
	
}
