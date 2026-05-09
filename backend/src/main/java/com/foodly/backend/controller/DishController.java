package com.foodly.backend.controller;

import com.foodly.backend.dto.DishResponseDto;
import com.foodly.backend.service.DishService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for dish-related endpoints. Provides endpoints for retrieving and
 * filtering dishes.
 */
@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DishController {

	private final DishService dishService;

	/**
	 * GET /api/dishes - Retrieve dishes with optional filtering and sorting.
	 * @param remainingKcal Optional: if provided, applies "Fit my day" filter (Story 2.1)
	 * @param sortBy Optional: "proteins" or "calories" for sorting (Story 2.6)
	 * @return List of DishResponseDto objects
	 */
	@GetMapping
	public ResponseEntity<List<DishResponseDto>> getDishes(
			@RequestParam(value = "remainingKcal", required = false) BigDecimal remainingKcal,
			@RequestParam(value = "sortBy", required = false) String sortBy) {

		List<DishResponseDto> dishes;

		if (remainingKcal != null) {
			// Apply "Fit my day" filter if remainingKcal is provided
			dishes = dishService.fitMyDay(remainingKcal, sortBy);
		}
		else {
			// Return all available dishes with optional sorting
			dishes = dishService.getAllDishes(sortBy);
		}

		return ResponseEntity.ok(dishes);
	}

}
