package com.hankki.domain.diet.converter;

import com.hankki.domain.diet.constant.MealType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class MealTypeConverter implements AttributeConverter<MealType, Integer>{

	@Override
	public Integer convertToDatabaseColumn(MealType attribute) {
		return (attribute != null ? attribute.getCode(): null);
	}

	@Override
	public MealType convertToEntityAttribute(Integer dbData) {
		return (dbData != null ? MealType.fromcode(dbData) : null);
	}

}
