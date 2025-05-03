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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="meal_item")
@NoArgsConstructor
@AllArgsConstructor
@Getter
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

	public MealItem(String foodName, MajorCategory majorCategory, double carbohydrate, double protein, double fat,
			double sugar) {
		this.foodName = foodName;
		this.majorCategory = majorCategory;
		this.carbohydrate = carbohydrate;
		this.protein = protein;
		this.fat = fat;
		this.sugar = sugar;
	}

	public void changeSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

	public void changeAmountStandard(int amountStandard) {
		this.amountStandard = amountStandard;
	}

	public void changeKcal(int kcal) {
		this.kcal = kcal;
	}

	public void changeMoisture(int moisture) {
		this.moisture = moisture;
	}

	public void changeCarbohydrate(double carbohydrate) {
		this.carbohydrate = carbohydrate;
	}

	public void changeProtein(double protein) {
		this.protein = protein;
	}

	public void changeFat(double fat) {
		this.fat = fat;
	}

	public void changeSugar(double sugar) {
		this.sugar = sugar;
	}

	public void changeSodium(double sodium) {
		this.sodium = sodium;
	}

	public void changeCholesterol(double cholesterol) {
		this.cholesterol = cholesterol;
	}
}
