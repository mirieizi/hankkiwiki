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
	
	@Column(name = "user_take_at")
	private LocalDate takeAt;
	
	@Column(name = "meal_type", nullable = false)
	@Convert(converter = MealTypeConverter.class)
	private MealType mealType;
	
	@Column(name = "meal_item", nullable = false)
	private List<MealItem> mealItems;

	@Column(name = "diet_memo")
	private String dietMemo;
	
	public Diet(String email, MealType mealType , List<MealItem> mealItems) {
		this.email = email;
		this.mealType = mealType;
		this.mealItems = mealItems;
	}

	public void changeTakeAt(LocalDate takeAt) {
		this.takeAt = takeAt;
	}

	public void changeMealType(MealType mealType) {
		this.mealType = mealType;
	}

	public void changeMealItems(List<MealItem> mealItems) {
		this.mealItems = mealItems;
	}

	public void changeDietMemo(String dietMemo) {
		this.dietMemo = dietMemo;
	}

}
