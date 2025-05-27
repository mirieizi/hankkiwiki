// src/test/java/com/hankki/domain/user/converter/GenderConverterTest.java
package com.hankki.domain.user.converter;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hankki.domain.user.constant.Gender;

class GenderConverterTest {

    private GenderConverter converter;

    @BeforeEach
    void setUp() {
        converter = new GenderConverter();
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertToDatabaseColumn_valid() {
        assertThat(converter.convertToDatabaseColumn(Gender.MALE)).isEqualTo("M");
        assertThat(converter.convertToDatabaseColumn(Gender.FEMALE)).isEqualTo("F");
    }

    @Test
    void convertToEntityAttribute_null() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void convertToEntityAttribute_valid() {
        assertThat(converter.convertToEntityAttribute("M")).isEqualTo(Gender.MALE);
        assertThat(converter.convertToEntityAttribute("F")).isEqualTo(Gender.FEMALE);
    }

    @Test
    void convertToEntityAttribute_invalid_shouldThrow() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("X"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown Gender code");
    }
}
