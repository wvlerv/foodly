package com.foodly.backend.service;

import com.foodly.backend.dto.HealthProfileDto;
import com.foodly.backend.dto.UserProfileDto;
import com.foodly.backend.entity.Dish;
import com.foodly.backend.entity.HealthProfile;
import com.foodly.backend.entity.User;
import com.foodly.backend.repository.DishRepository;
import com.foodly.backend.repository.HealthProfileRepository;
import com.foodly.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

	private final HealthProfileRepository profileRepository;

	private final UserRepository userRepository;

	private final DishRepository dishRepository;

	private final NutritionService nutritionService;

	@Transactional
	public HealthProfile updateProfile(HealthProfileDto dto, String email) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		if (dto.getFirstName() != null) {
			user.setFirstName(dto.getFirstName());
		}
		if (dto.getLastName() != null) {
			user.setLastName(dto.getLastName());
		}
		if (dto.getUsername() != null) {
			user.setUsername(dto.getUsername());
		}

		HealthProfile profile = user.getHealthProfile();

		if (profile == null) {
			profile = new HealthProfile();
			profile.setId(user.getId());
			profile.setUser(user);
			user.setHealthProfile(profile);
		}

		profile.setAge(dto.getAge());
		profile.setWeight(dto.getWeight());
		profile.setHeight(dto.getHeight());
		profile.setGender(dto.getGender());
		profile.setActivityMultiplier(dto.getActivityMultiplier());
		profile.setTarget(dto.getTarget());
		profile.setAllergens(dto.getAllergens());
		BigDecimal dci = nutritionService.calculateFullDci(profile);
		profile.setDailyCalorieIntake(dci);

		userRepository.save(user);

		return user.getHealthProfile();
	}

	@Transactional(readOnly = true)
	public UserProfileDto getProfileByEmail(String email) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("User not found for email: " + email));
		return UserProfileDto.from(user);
	}

	/**
	 * Get all favorite dishes for the current authenticated user.
	 * @param email the email of the authenticated user
	 * @return list of favorite dishes
	 */
	@Transactional(readOnly = true)
	public List<Dish> getFavoriteDishes(String email) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
		return user.getFavoriteDishes();
	}

	/**
	 * Add a dish to the user's favorites.
	 * @param email the email of the authenticated user
	 * @param dishId the ID of the dish to add to favorites
	 */
	@Transactional
	public void addToFavorites(String email, UUID dishId) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		Dish dish = dishRepository.findById(dishId)
			.orElseThrow(() -> new RuntimeException("Dish not found with id: " + dishId));

		if (!user.getFavoriteDishes().contains(dish)) {
			user.getFavoriteDishes().add(dish);
			userRepository.save(user);
		}
	}

	/**
	 * Remove a dish from the user's favorites.
	 * @param email the email of the authenticated user
	 * @param dishId the ID of the dish to remove from favorites
	 */
	@Transactional
	public void removeFromFavorites(String email, UUID dishId) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		Dish dish = dishRepository.findById(dishId)
			.orElseThrow(() -> new RuntimeException("Dish not found with id: " + dishId));

		if (user.getFavoriteDishes().contains(dish)) {
			user.getFavoriteDishes().remove(dish);
			userRepository.save(user);
		}
	}

}