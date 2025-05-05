package com.hankki.domain.diet.entity;

import java.time.LocalDate;
import java.util.List;

import com.hankki.domain.diet.constant.MealType;
import com.hankki.domain.diet.converter.MealTypeConverter;

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
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED) // 외부 생성은 차단하여 빌더와 충돌 방지
@Builder
public class Diet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "user_email", unique = true, nullable = false, updatable = false)
	private String email;
	
	@Column(name = "user_take_at")
	private LocalDate takeAt;
	
	@Column(name = "meal_type", nullable = false)
	@Convert(converter = MealTypeConverter.class)
	private MealType mealType;

	@Column(name = "diet_memo")
	private String dietMemo;
	
	public Diet(String email, MealType mealType) {
		this.email = email;
		this.mealType = mealType;
	}

	protected void setId(Long id) {
		this.id = id;
	}

	protected void setEmail(String email) {
		this.email = email;
	}

	protected void setTakeAt(LocalDate takeAt) {
		this.takeAt = takeAt;
	}

	protected void setMealType(MealType mealType) {
		this.mealType = mealType;
	}

	protected void setDietMemo(String dietMemo) {
		this.dietMemo = dietMemo;
	}

}
