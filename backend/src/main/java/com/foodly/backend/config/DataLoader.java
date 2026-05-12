package com.foodly.backend.config;

import com.foodly.backend.entity.NutrientLog; // Перевір, чи саме так називається твоя сутність
import com.foodly.backend.repository.NutrientLogRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initNutritionData(NutrientLogRepository repository) {
        return args -> {
            // Наповнюємо лише якщо база порожня
            if (repository.count() == 0) {
                repository.saveAll(List.of(
                        new NutrientLog(LocalDate.now().minusDays(6), 1850),
                        new NutrientLog(LocalDate.now().minusDays(5), 2100),
                        new NutrientLog(LocalDate.now().minusDays(4), 1950),
                        new NutrientLog(LocalDate.now().minusDays(3), 2250),
                        new NutrientLog(LocalDate.now().minusDays(2), 2000),
                        new NutrientLog(LocalDate.now().minusDays(1), 2150),
                        new NutrientLog(LocalDate.now(), 1900)
                ));
                System.out.println("Database seeded: Nutrition logs created for the chart!");
            }
        };
    }
}