package com.foodly.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class representing a dish in the menu. Contains nutritional information and
 * allergen data.
 */
@Entity
@Table(name = "dishes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dish {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(length = 1000)
	private String description;

	@Column(nullable = false)
	private BigDecimal price;

	private BigDecimal weight;

	@Column(name = "image_url")
	private String imageUrl;

	@Column(name = "is_available")
	@Builder.Default
	private boolean isAvailable = true;

	private BigDecimal calories;

	private BigDecimal proteins;

	private BigDecimal fats;

	private BigDecimal carbohydrates;

	@ElementCollection
	private List<String> allergens;

	@OneToMany(mappedBy = "dish", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<DishIngredient> recipe = new ArrayList<>();

	/**
	 * Adds an ingredient to the dish recipe.
	 * @param ingredient the ingredient to add
	 */
	public void addIngredient(DishIngredient ingredient) {
		recipe.add(ingredient);
		ingredient.setDish(this);
	}

}
