package com.foodly.backend.service;

import org.springframework.stereotype.Service;

@Service
public class NutritionService {
    public double calculateBMR(double weight, double height, int age, String gender) {
        if (age <= 0 || weight <= 0) throw new IllegalArgumentException("Invalid input data");

        double bmr = (10 * weight) + (6.25 * height) - (5 * age);
        return gender.equalsIgnoreCase("male") ? bmr + 5 : bmr - 161;
    }

    public double calculateDCI(double bmr, String target) {
        switch (target.toLowerCase()) {
            case "lose": return bmr * 0.85;    // Дефіцит 15%
            case "gain": return bmr * 1.15;    // Профіцит 15%
            case "maintain": return bmr;       // Підтримка
            default: throw new IllegalArgumentException("Unknown target");
        }
    }
}