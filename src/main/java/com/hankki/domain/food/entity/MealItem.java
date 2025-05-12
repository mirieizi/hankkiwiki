package com.hankki.domain.food.entity;

import com.hankki.domain.food.constant.MajorCategory;
import com.hankki.domain.food.constant.MajorCategoryConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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

}
