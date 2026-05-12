package com.foodly.backend.controller;

import com.foodly.backend.dto.NutritionResponse; // Потрібно буде створити цей DTO
import com.foodly.backend.entity.NutrientLog;
import com.foodly.backend.repository.NutrientLogRepository;
import com.foodly.backend.service.NutritionService; // Імпортуємо твій сервіс
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
@Tag(name = "Nutrition Controller", description = "Управління щоденником харчування")
@CrossOrigin(origins = "http://localhost:3000")
public class NutritionController {

    private final NutrientLogRepository repository;
    private final NutritionService nutritionService; // 1. Додаємо сервіс сюди

    @GetMapping("/logs")
    @Operation(summary = "Отримати всі записи щоденника харчування та норму калорій")
    public ResponseEntity<NutritionResponse> getAllLogs() { // 2. Змінюємо List на NutritionResponse
        // Отримуємо всі записи з бази
        List<NutrientLog> logs = repository.findAll();

        // 3. Викликаємо методи розрахунку з твого сервісу
        // Поки що дані (вага, зріст, вік) впишемо вручну, пізніше вони будуть з бази
        double bmr = nutritionService.calculateBmr(70.0, 175.0, 25, "male");
        double dailyGoal = nutritionService.calculateDci(bmr, "maintain");

        // Повертаємо об'єднану відповідь
        return ResponseEntity.ok(new NutritionResponse(logs, dailyGoal));
    }
}