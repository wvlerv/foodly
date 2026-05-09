package com.foodly.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class representing a food ingredient with nutritional information.
 */
@Entity
@Table(name = "ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true)
	private String name;

	@Column(name = "calories_per_100g")
	private BigDecimal caloriesPer100g;

	@Column(name = "proteins_per_100g")
	private BigDecimal proteinsPer100g;

	@Column(name = "fats_per_100g")
	private BigDecimal fatsPer100g;

	@Column(name = "carbs_per_100g")
	private BigDecimal carbsPer100g;

}
