package com.hankki.domain.user.converter;

import com.hankki.domain.auth.constant.Role;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String>{

	/** 엔티티 -> DB 컬럼 */
	@Override
	public String convertToDatabaseColumn(Role attribute) {
		return (attribute !=null ? attribute.getCode() : null);
	}

	/** DB 컬럼 -> 엔티티 */
	@Override
	public Role convertToEntityAttribute(String dbData) {
		return (dbData !=null ? Role.fromCode(dbData) : null);
	}

}
