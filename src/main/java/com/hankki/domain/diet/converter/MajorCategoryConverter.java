package com.hankki.domain.diet.converter;

import com.hankki.domain.diet.constant.MajorCategory;
import com.hankki.domain.diet.constant.MealType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class MajorCategoryConverter implements AttributeConverter<MajorCategory, Integer> {

	@Override
	public Integer convertToDatabaseColumn(MajorCategory attribute) {
		return (attribute != null ? attribute.getCode(): null);
	}

	@Override
	public MajorCategory convertToEntityAttribute(Integer dbData) {
		return (dbData != null ? MajorCategory.fromcode(dbData) : null);
	}
}
