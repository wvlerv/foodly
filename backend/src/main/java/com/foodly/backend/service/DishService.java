package com.foodly.backend.service;

import io.micrometer.core.instrument.Counter;
import com.foodly.backend.dto.DishResponseDto;
import com.foodly.backend.entity.Dish;
import com.foodly.backend.repository.DishRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

	private Counter getFilterCounter() {
		return meterRegistry.counter("foodly_filter_used_total");
	}

	/**
	 * Retrieve all available dishes with optional sorting.
	 * @param sortBy "proteins" or "calories" to sort by that field; null for default
	 * (name)
	 * @return List of available dishes as DTOs
	 */
	public List<DishResponseDto> getAllDishes(String sortBy, boolean isManagerOrAdmin) {
		List<Dish> dishes;
		List<String> allowedSorts = List.of("proteins", "calories", "fats", "carbs");

		if (sortBy != null && allowedSorts.contains(sortBy.toLowerCase())) {
			dishes = isManagerOrAdmin
					? dishRepository.findAllSortedForAdmin(sortBy.toLowerCase())
					: dishRepository.findAllAvailableSorted(sortBy.toLowerCase());
		}
		else {
			dishes = isManagerOrAdmin
					? dishRepository.findAllForAdmin()
					: dishRepository.findAllAvailable();
		}

		logger.info("Catalog loaded: {} dishes (Admin mode: {})", dishes.size(), isManagerOrAdmin);
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
		if (remainingKcal != null && remainingKcal.compareTo(BigDecimal.ZERO) <= 0) {
			logger.warn("User attempted to filter with zero or negative calories: {}", remainingKcal);
		}
		getFilterCounter().increment();

		List<Dish> dishes;

		List<String> allowedSorts = List.of("proteins", "calories", "fats", "carbs");
		if (sortBy != null && allowedSorts.contains(sortBy.toLowerCase())) {
			dishes = dishRepository.findAllAvailableSorted(sortBy.toLowerCase());
		}
		else {
			dishes = dishRepository.findAllAvailable();
		}

		if (remainingKcal == null) {
			return dishes.stream().map(DishResponseDto::from).collect(Collectors.toList());
		}

		List<DishResponseDto> filtered = dishes.stream().filter(dish -> {
			if (dish.getCalories() == null)
				return false;
			return dish.getCalories()
				.setScale(0, RoundingMode.HALF_UP)
				.compareTo(remainingKcal.setScale(0, RoundingMode.HALF_UP)) <= 0;
		}).map(DishResponseDto::from).collect(Collectors.toList());

		logger.info("'Fit my day' applied: {} dishes found for limit {}", filtered.size(), remainingKcal);
		return filtered;
	}

	@Transactional
	public void toggleAvailability(UUID dishId, boolean available) {
		Dish dish = dishRepository.findById(dishId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dish not found"));

		dish.setAvailable(available);
	}
}
