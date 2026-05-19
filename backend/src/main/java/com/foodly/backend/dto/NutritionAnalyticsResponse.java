package com.foodly.backend.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionAnalyticsResponse {

    private BigDecimal dailyGoal;
    private List<CalorieLogDto> logs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalorieLogDto {
        private String date;
        private int consumedCalories;
    }
}