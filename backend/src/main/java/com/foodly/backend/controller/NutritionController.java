package com.foodly.backend.controller;

import com.foodly.backend.dto.NutritionAnalyticsResponse;
import com.foodly.backend.service.NutritionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NutritionController {

	private final NutritionService nutritionService;

	@GetMapping("/logs")
	public ResponseEntity<?> getPlayerNutritionAnalytics(Authentication authentication) {
		try {
			if (authentication == null || !authentication.isAuthenticated()) {
				return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
					.body("Not authenticated");
			}

			String email = authentication.getName(); // Витягуємо email з JWT токена
			NutritionAnalyticsResponse analytics = nutritionService.getUserAnalytics(email);

			return ResponseEntity.ok(analytics);
		}
		catch (Exception e) {
			return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
				.body("Failed to load nutrition analytics: " + e.getMessage());
		}
	}

}