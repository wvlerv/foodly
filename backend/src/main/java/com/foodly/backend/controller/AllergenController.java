package com.foodly.backend.controller;

import com.foodly.backend.service.AllergenService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for retrieving available allergens from menu dishes.
 */
@RestController
@RequestMapping("/api/allergens")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AllergenController {

	private final AllergenService allergenService;

	/**
	 * GET /api/allergens - returns all unique allergens used by dishes.
	 * @return response with the list of allergens
	 */
	@GetMapping
	public ResponseEntity<List<String>> getAllergens() {
		return ResponseEntity.ok(allergenService.getAllergens());
	}

}