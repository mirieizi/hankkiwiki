package com.hankki.domain.diet.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hankki.domain.diet.constant.MealType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "diet_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED) // 외부 생성은 차단하여 빌더와 충돌 방지
@Builder
public class DietGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "user_id", nullable = false, updatable = false)
	private Long userId;
	
	@Column(name = "take_at", nullable = false)
	private LocalDate takeAt;

	@Column(
			name = "meal_type",
			nullable = false,
			columnDefinition = "TINYINT NOT NULL DEFAULT 9"
	)
	@Convert(converter = MealType.MealTypeConverter.class)
	private MealType mealType = MealType.fromCode(9);

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}
}
