package com.hankki.domain.diet.entity;

import java.time.LocalDate;
import java.util.List;

import com.hankki.domain.diet.config.MealTypeConverter;
import com.hankki.domain.diet.dto.MealType;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_diet")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Diet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "user_email", unique = true, nullable = false, updatable = false)
	private String email;
	
	@Column(name = "user_take_at", nullable = true, updatable = true)
	private LocalDate takeAt;
	
	@Column(name = "meal_type", nullable = false)
	@Convert(converter = MealTypeConverter.class)
	private MealType mealType;
	
	@Column(name = "meal_item", nullable = false)
	private List<MealItem> mealItems;
	
	public Diet(String email, MealType mealType , List<MealItem> mealItems) {
		this.email = email;
		this.mealType = mealType;
		this.mealItems = mealItems;
	}

	public void updateTakeAt(LocalDate takeAt) {
		this.takeAt = takeAt;
	}

	public void updateMeal(MealType mealType) {
		this.mealType = mealType;
	}
	
}
