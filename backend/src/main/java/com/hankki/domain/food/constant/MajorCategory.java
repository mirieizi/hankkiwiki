package com.hankki.domain.food.constant;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;

import lombok.Getter;

@Getter
public enum MajorCategory {
	
	RICE(1, "밥류"),
	BREAD_AND_SNACKS(2, "빵 및 과자류"),
	NOODLES_AND_DUMPLINGS(3, "면 및 만두류"),
	PORRIDGE_AND_SOUP(4, "죽 및 스프류"),
	SOUP_AND_STEW(5, "국 및 탕류"),
	HOT_POT_AND_JJIGAE(6, "찌개 및 전골류"),
	STEAMED_DISHES(7, "찜류"),
	GRILLED_DISHES(8, "구이류"),
	PAN_FRIED_AND_JEON(9, "전/적 및 부침류"),
	STIR_FRIED(10, "볶음류"),
	BRAISED_DISHES(11, "조림류"),
	FRIED_DISHES(12, "튀김류"),
	VEGETABLE_SIDE_DISH(13, "나물/숙채류"),
	RAW_AND_SEASONED(14, "생채/무침류"),
	KIMCHI(15, "김치류"),
	FERMENTED_SEAFOOD(16, "젓갈류"),
	PICKLES_AND_PRESERVES(17, "장아찌/절임류"),
	SAUCES_AND_SEASONINGS(18, "장류/양념류"),
	DAIRY_AND_ICE_CREAM(19, "유제품류 및 빙과류"),
	BEVERAGE_AND_TEA(20, "음료 및 차류"),
	GRAIN_AND_POTATO(24, "곡류/서류 제품"),
	VEGETABLES_AND_SEAWEED(26, "채소/해조류"),
	AQUATIC_AND_MEAT(27, "수/조/어/육류");

	
	private final int code;
	private final String description;
	
	private MajorCategory(int code, String description) {
		this.code = code;
		this.description = description;
	}
	
	public static MajorCategory fromcode(int code) {
		for (MajorCategory major : values()) {
			if (major.code == code) {
				return major;
			}
		}
		throw new HankkiWikiException(ExceptionStatus.INVALID_MAJOR_CATEGORY);
	}
	
}
