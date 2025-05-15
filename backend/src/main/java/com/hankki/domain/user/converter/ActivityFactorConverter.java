package com.hankki.domain.user.converter;

import com.hankki.domain.user.constant.ActivityFactor;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ActivityFactorConverter implements jakarta.persistence.AttributeConverter<ActivityFactor, Double>{

	@Override
	public Double convertToDatabaseColumn(ActivityFactor attribute) {
		return (attribute != null ? attribute.getCoefficient() : null);
	}

	@Override
	public ActivityFactor convertToEntityAttribute(Double dbData) {
		return (dbData != null ? ActivityFactor.fromCoefficient(dbData) : null); 
	}
	

}
