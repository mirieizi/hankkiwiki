// src/test/java/com/hankki/domain/user/converter/ActivityFactorConverterTest.java
package com.hankki.domain.user.converter;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hankki.domain.user.constant.ActivityFactor;

class ActivityFactorConverterTest {

    private ActivityFactorConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ActivityFactorConverter();
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertToDatabaseColumn_valid() {
        assertThat(converter.convertToDatabaseColumn(ActivityFactor.SEDENTARY))
            .isEqualTo(0.20);
        assertThat(converter.convertToDatabaseColumn(ActivityFactor.VERY_ACTIVE))
            .isEqualTo(0.90);
    }

    @Test
    void convertToEntityAttribute_null() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void convertToEntityAttribute_valid() {
        assertThat(converter.convertToEntityAttribute(0.20))
            .isEqualTo(ActivityFactor.SEDENTARY);
        assertThat(converter.convertToEntityAttribute(0.90))
            .isEqualTo(ActivityFactor.VERY_ACTIVE);
    }

    @Test
    void convertToEntityAttribute_invalid_shouldThrow() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute(1.00))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("UnKnown coefficient");
    }
}
