package com.foodly.backend.controller;

import com.foodly.backend.dto.HealthProfileDto;
import com.foodly.backend.entity.Dish;
import com.foodly.backend.entity.HealthProfile;
import com.foodly.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

	private final ProfileService profileService;

	@PostMapping("/update")
	public ResponseEntity<?> updateProfile(@RequestBody HealthProfileDto dto, Authentication authentication) {
		HealthProfile updated = profileService.updateProfile(dto, authentication.getName());

		return ResponseEntity.ok(Map.of("message", "Profile updated successfully", "dailyCalories",
				updated.getDailyCalorieIntake().setScale(0, BigDecimal.ROUND_HALF_UP), "userId", updated.getId()));
	}

	@GetMapping("/me")
	public ResponseEntity<?> getMyProfile(Authentication authentication) {
		log.info("Fetching profile for: {}", authentication.getName());
		return ResponseEntity.ok(profileService.getProfileByEmail(authentication.getName()));
	}

	/**
	 * Get all favorite dishes for the authenticated user.
	 * @param authentication Spring Security authentication object
	 * @return list of favorite dishes
	 */
	@GetMapping("/favorites")
	public ResponseEntity<List<Dish>> getFavoriteDishes(Authentication authentication) {
		try {
			if (authentication == null || !authentication.isAuthenticated()) {
				log.warn("Unauthorized access to /api/profile/favorites");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}

			log.info("Fetching favorite dishes for: {}", authentication.getName());
			List<Dish> favorites = profileService.getFavoriteDishes(authentication.getName());
			return ResponseEntity.ok(favorites);
		}
		catch (RuntimeException e) {
			log.error("Error fetching favorites: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Add a dish to the user's favorites.
	 * @param dishId the ID of the dish to add
	 * @param authentication Spring Security authentication object
	 * @return success message
	 */
	@PostMapping("/favorites/{dishId}")
	public ResponseEntity<?> addToFavorites(@PathVariable UUID dishId, Authentication authentication) {
		try {
			if (authentication == null || !authentication.isAuthenticated()) {
				log.warn("Unauthorized access to POST /api/profile/favorites/{}", dishId);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
			}

			log.info("Adding dish {} to favorites for user: {}", dishId, authentication.getName());
			profileService.addToFavorites(authentication.getName(), dishId);
			return ResponseEntity.ok(Map.of("message", "Dish added to favorites successfully"));
		}
		catch (RuntimeException e) {
			log.error("Error adding to favorites: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
		}
		catch (Exception e) {
			log.error("Unexpected error adding to favorites: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("error", "Internal server error"));
		}
	}

	/**
	 * Remove a dish from the user's favorites.
	 * @param dishId the ID of the dish to remove
	 * @param authentication Spring Security authentication object
	 * @return success message
	 */
	@DeleteMapping("/favorites/{dishId}")
	public ResponseEntity<?> removeFromFavorites(@PathVariable UUID dishId, Authentication authentication) {
		try {
			if (authentication == null || !authentication.isAuthenticated()) {
				log.warn("Unauthorized access to DELETE /api/profile/favorites/{}", dishId);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
			}

			log.info("Removing dish {} from favorites for user: {}", dishId, authentication.getName());
			profileService.removeFromFavorites(authentication.getName(), dishId);
			return ResponseEntity.ok(Map.of("message", "Dish removed from favorites successfully"));
		}
		catch (RuntimeException e) {
			log.error("Error removing from favorites: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
		}
		catch (Exception e) {
			log.error("Unexpected error removing from favorites: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("error", "Internal server error"));
		}
	}

}
