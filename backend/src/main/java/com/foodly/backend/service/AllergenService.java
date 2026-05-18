package com.foodly.backend.service;

import com.foodly.backend.repository.DishRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for resolving available allergens from dishes stored in the database.
 */
@Service
@RequiredArgsConstructor
public class AllergenService {

	private final DishRepository dishRepository;

	/**
	 * Returns all unique allergens used across all dishes.
	 * @return cleaned list of allergen names
	 */
	public List<String> getAllergens() {
		return dishRepository.findDistinctAllergens()
			.stream()
			.filter(allergen -> allergen != null)
			.map(String::trim)
			.filter(allergen -> !allergen.isEmpty())
			.filter(allergen -> !"string".equalsIgnoreCase(allergen))
			.distinct()
			.collect(Collectors.toList());
	}

}