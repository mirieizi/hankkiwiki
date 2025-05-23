package com.hankki.domain.diet.entity;

import java.time.LocalDate;

import com.hankki.domain.diet.constant.MealType;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "diet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED) // 외부 생성은 차단하여 빌더와 충돌 방지
@Builder
public class Diet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "user_id", nullable = false, updatable = false)
	private Long userId;
	
	@Column(name = "take_at")
	private LocalDate takeAt;

	@Column(
			name = "meal_type",
			nullable = false,
			columnDefinition = "TINYINT NOT NULL DEFAULT 9"
	)
	@Convert(converter = MealType.MealTypeConverter.class)
	private MealType mealType = MealType.fromCode(9);

}
