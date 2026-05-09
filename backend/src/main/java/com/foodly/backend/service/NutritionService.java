package com.foodly.backend.service;

import org.springframework.stereotype.Service;

/**
 * Service for calculating nutritional metrics like BMR (Basal Metabolic Rate) and DCI
 * (Daily Caloric Intake).
 */
@Service
public class NutritionService {

	/**
	 * Calculates the Basal Metabolic Rate (BMR) using the Mifflin-St Jeor equation.
	 * @param weight the weight in kilograms
	 * @param height the height in centimeters
	 * @param age the age in years
	 * @param gender "male" or "female"
	 * @return the calculated BMR value
	 * @throws IllegalArgumentException if age or weight is invalid
	 */
	public double calculateBmr(double weight, double height, int age, String gender) {
		if (age <= 0 || weight <= 0) {
			throw new IllegalArgumentException("Invalid input data");
		}

		double bmr = (10 * weight) + (6.25 * height) - (5 * age);
		return gender.equalsIgnoreCase("male") ? bmr + 5 : bmr - 161;
	}

	/**
	 * Calculates the Daily Caloric Intake (DCI) based on BMR and target.
	 * @param bmr the basal metabolic rate
	 * @param target the weight target ("lose", "gain", or "maintain")
	 * @return the calculated daily caloric intake
	 * @throws IllegalArgumentException if the target is unknown
	 */
	public double calculateDci(double bmr, String target) {
		switch (target.toLowerCase()) {
			case "lose":
				return bmr * 0.85;
			case "gain":
				return bmr * 1.15;
			case "maintain":
				return bmr;
			default:
				throw new IllegalArgumentException("Unknown target");
		}
	}

}
