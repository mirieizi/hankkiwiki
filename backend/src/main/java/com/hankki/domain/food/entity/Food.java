package com.hankki.domain.food.entity;

import jakarta.persistence.Column;
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
@Table(name = "food")
public class Food {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String foodName;
	
	@Column(nullable = false)
	private String majorCategory;
	
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
