package com.foodly.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class representing a nutrient consumption log for a user on a specific date.
 */
@Entity
@Table(name = "nutrient_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutrientLog {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	private LocalDate date;

	private BigDecimal consumedCalories;

	private BigDecimal consumedProteins;

	private BigDecimal consumedFats;

	private BigDecimal consumedCarbs;

	// Спеціальний конструктор для DataLoader
	public NutrientLog(LocalDate date, Integer calories) {
		this.date = date;
		// Перетворюємо Integer у BigDecimal
		this.consumedCalories = calories != null ? BigDecimal.valueOf(calories) : null;
	}
}
