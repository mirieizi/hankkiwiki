package com.hankki.domain.user.converter;

import com.hankki.domain.user.constant.Gender;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<Gender, String> {

	@Override
	public String convertToDatabaseColumn(Gender attribute) {
		return (attribute != null ? attribute.getCode() : null);
	}

	@Override
	public Gender convertToEntityAttribute(String dbData) {
		return (dbData != null ? Gender.fromCode(dbData) : null);
	}

}
