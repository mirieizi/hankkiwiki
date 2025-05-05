package com.hankki.domain.diet.entity;

import com.hankki.domain.diet.constant.MajorCategory;
import com.hankki.domain.diet.converter.MajorCategoryConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(name = "meal_item")
public class MealItem {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String foodName;
	
	@Column(nullable = false)
	@Convert(converter = MajorCategoryConverter.class)
	private MajorCategory majorCategory;
	
	@Column
	private String subCategory;
	
	@Column
	private int amountStandard;
	
	@Column
	private int kcal;
	
	@Column
	private int moisture;
	
	@Column(nullable = false)
	private double carbohydrate; 

	@Column(nullable = false)
	private double protein;
	
	@Column(nullable = false)
	private double fat;
	
	@Column(nullable = false)
	private double sugar;
	
	@Column
	private double sodium; 
	
	@Column
	private double cholesterol;

	protected void setId(Long id) {
		this.id = id;
	}

	protected void setFoodName(String foodName) {
		this.foodName = foodName;
	}

	protected void setMajorCategory(MajorCategory majorCategory) {
		this.majorCategory = majorCategory;
	}

	protected void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

	protected void setAmountStandard(int amountStandard) {
		this.amountStandard = amountStandard;
	}

	protected void setKcal(int kcal) {
		this.kcal = kcal;
	}

	protected void setMoisture(int moisture) {
		this.moisture = moisture;
	}

	protected void setCarbohydrate(double carbohydrate) {
		this.carbohydrate = carbohydrate;
	}

	protected void setProtein(double protein) {
		this.protein = protein;
	}

	protected void setFat(double fat) {
		this.fat = fat;
	}

	protected void setSugar(double sugar) {
		this.sugar = sugar;
	}

	protected void setSodium(double sodium) {
		this.sodium = sodium;
	}

	protected void setCholesterol(double cholesterol) {
		this.cholesterol = cholesterol;
	}
}
