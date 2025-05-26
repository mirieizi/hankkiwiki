package com.hankki.domain.user.constant;

public enum Gender {
	MALE("M"), // 남성
	FEMALE("F"); // 여성
	
	private final String code;

	Gender(String code) {this.code = code;}

	public String getCode() {
		return code;
	}
	
	public static Gender fromCode(String dbCode) {
		for (Gender g : values()) {
			if (g.code.equals(dbCode)) return g;
		}
		throw new IllegalArgumentException("Unknown Gender code: " + dbCode);

	}

	// Redis, 인덱스 명 등에 사용할 소문자 문자열 변환용
	public String key() {
		return name().toLowerCase();
	}
}
