package com.foodly.backend.service;

import com.foodly.backend.dto.DishResponseDto;
import com.foodly.backend.entity.Dish;
import com.foodly.backend.repository.DishRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for managing dishes and applying filters. Implements the "Fit my day" logic
 * (Story 2.1) with BigDecimal precision (Fix Bug #12). Integrates SLF4J logging and
 * Micrometer metrics (Lab 3).
 */
@Service
@RequiredArgsConstructor
public class DishService {

	private static final Logger logger = LoggerFactory.getLogger(DishService.class);

	private final DishRepository dishRepository;

	private final MeterRegistry meterRegistry;

	/**
	 * Retrieve all available dishes with optional sorting.
	 * @param sortBy "proteins" or "calories" to sort by that field; null for default
	 * (name)
	 * @return List of available dishes as DTOs
	 */
	public List<DishResponseDto> getAllDishes(String sortBy) {
		List<Dish> dishes;

		List<String> allowedSorts = List.of("proteins", "calories", "fats", "carbs");

		if (sortBy != null && allowedSorts.contains(sortBy.toLowerCase())) {
			dishes = dishRepository.findAllAvailableSorted(sortBy.toLowerCase());
		}
		else {
			dishes = dishRepository.findAllAvailable();
		}

		logger.info("Catalog loaded: {} dishes, sorted by: {}", dishes.size(), sortBy != null ? sortBy : "default");
		return dishes.stream().map(DishResponseDto::from).collect(Collectors.toList());
	}

	/**
	 * Implements the "Fit my day" filter (Story 2.1). Filters dishes where calories <=
	 * remainingKcal using BigDecimal precision (Fix Bug #12).
	 * @param remainingKcal The remaining kilocalories available for the user's day
	 * @param sortBy "proteins" or "calories" to sort by that field; null for default
	 * (name)
	 * @return List of dishes that fit within the remaining kilocalories
	 */
	public List<DishResponseDto> fitMyDay(BigDecimal remainingKcal, String sortBy) {
		List<Dish> dishes;

		List<String> allowedSorts = List.of("proteins", "calories", "fats", "carbs");
		if (sortBy != null && allowedSorts.contains(sortBy.toLowerCase())) {
			dishes = dishRepository.findAllAvailableSorted(sortBy.toLowerCase());
		}
		else {
			dishes = dishRepository.findAllAvailable();
		}

		if (remainingKcal == null) {
			logger.info("'Fit my day' inactive (null). Returning all dishes.");
			return dishes.stream().map(DishResponseDto::from).collect(Collectors.toList());
		}

		List<DishResponseDto> filtered = dishes.stream().filter(dish -> {
			if (dish.getCalories() == null) {
				return false;
			}

			BigDecimal caloriesRounded = dish.getCalories().setScale(0, RoundingMode.HALF_UP);
			BigDecimal remainingRounded = remainingKcal.setScale(0, RoundingMode.HALF_UP);

			return caloriesRounded.compareTo(remainingRounded) <= 0;
		}).map(DishResponseDto::from).collect(Collectors.toList());

		logger.info("'Fit my day' applied: {} dishes found for limit {}", filtered.size(), remainingKcal);
		return filtered;
	}

}
