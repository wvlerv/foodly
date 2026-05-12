package com.foodly.backend.dto;

import com.foodly.backend.entity.NutrientLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class NutritionResponse {
    private List<NutrientLog> logs;
    private double dailyGoal;
}