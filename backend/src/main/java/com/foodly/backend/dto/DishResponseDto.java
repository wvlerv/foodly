package com.foodly.backend.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Dish entity. Maps the Dish entity for frontend consumption.
 * Includes allergens for allergen warning display (Story 2.2).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishResponseDto implements Serializable {

	private UUID id;

	private String name;

	private String description;

	private BigDecimal price;

	private BigDecimal weight;

	private String imageUrl;

	private boolean available;

	// Nutrition facts
	private BigDecimal calories;

	private BigDecimal proteins;

	private BigDecimal fats;

	private BigDecimal carbohydrates;

	// Allergens for rendering warnings (Story 2.2)
	private List<String> allergens;

	/**
	 * Converts a Dish entity to DishResponseDto.
	 * @param dish the Dish entity to convert
	 * @return a new DishResponseDto instance
	 */
	public static DishResponseDto from(com.foodly.backend.entity.Dish dish) {
		return DishResponseDto.builder()
			.id(dish.getId())
			.name(dish.getName())
			.description(dish.getDescription())
			.price(dish.getPrice())
			.weight(dish.getWeight())
			.imageUrl(dish.getImageUrl())
			.available(dish.isAvailable())
			.calories(dish.getCalories())
			.proteins(dish.getProteins())
			.fats(dish.getFats())
			.carbohydrates(dish.getCarbohydrates())
			.allergens(dish.getAllergens())
			.build();
	}

}
