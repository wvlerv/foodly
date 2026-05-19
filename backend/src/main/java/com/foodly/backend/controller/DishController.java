package com.foodly.backend.controller;

import com.foodly.backend.dto.DishResponseDto;
import com.foodly.backend.service.DishService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
			@RequestParam(value = "sortBy", required = false) String sortBy,
			Authentication authentication) { // Додаємо об'єкт Authentication
		boolean isManagerOrAdmin = authentication != null &&
				(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER")) ||
						authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));

		List<DishResponseDto> dishes;

		if (remainingKcal != null) {

			dishes = dishService.fitMyDay(remainingKcal, sortBy);
		}
		else {
			dishes = dishService.getAllDishes(sortBy, isManagerOrAdmin);
		}

		return ResponseEntity.ok(dishes);
	}

	@PutMapping("/{id}/toggle-availability")
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	public ResponseEntity<?> toggleDishAvailability(@PathVariable UUID id, @RequestParam boolean available) {
		dishService.toggleAvailability(id, available);
		String message = available ? "Dish is now available" : "Dish added to stop-list";
		return ResponseEntity.ok(Map.of("message", message));
	}
}
