package com.foodly.backend.service;

import com.foodly.backend.entity.NutrientLog;
import com.foodly.backend.entity.Order;
import com.foodly.backend.entity.OrderItem;
import com.foodly.backend.repository.NutrientLogRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Service for calculating nutritional metrics like BMR (Basal Metabolic Rate) and DCI
 * (Daily Caloric Intake).
 */

@Slf4j
@Service
public class NutritionService {

	private final NutrientLogRepository nutrientLogRepository;
	private final Counter nutritionUpdatesCounter;

	public NutritionService(NutrientLogRepository nutrientLogRepository, MeterRegistry registry) {
		this.nutrientLogRepository = nutrientLogRepository;
		this.nutritionUpdatesCounter = Counter.builder("foodly_nutrition_updates_total")
				.description("Кількість оновлень щоденника харчування")
				.register(registry);
	}

	@Transactional
	public void recordNutritionFromOrder(Order order) {
		log.info("Processing nutrition for order: {}", order.getId());

		BigDecimal totalKcal = BigDecimal.ZERO;
		BigDecimal totalProteins = BigDecimal.ZERO;
		BigDecimal totalFats = BigDecimal.ZERO;
		BigDecimal totalCarbs = BigDecimal.ZERO;

		for (OrderItem item : order.getItems()) {
			BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());

			totalKcal = totalKcal.add(item.getDish().getCalories().multiply(quantity));
			totalProteins = totalProteins.add(item.getDish().getProteins().multiply(quantity));
			totalFats = totalFats.add(item.getDish().getFats().multiply(quantity));
			totalCarbs = totalCarbs.add(item.getDish().getCarbohydrates().multiply(quantity));
		}

		NutrientLog dailyLog = nutrientLogRepository
				.findByUserAndDate(order.getUser(), LocalDate.now())
				.orElse(NutrientLog.builder()
						.user(order.getUser())
						.date(LocalDate.now())
						.consumedCalories(BigDecimal.ZERO)
						.consumedProteins(BigDecimal.ZERO)
						.consumedFats(BigDecimal.ZERO)
						.consumedCarbs(BigDecimal.ZERO)
						.build());

		dailyLog.setConsumedCalories(dailyLog.getConsumedCalories().add(totalKcal));
		dailyLog.setConsumedProteins(dailyLog.getConsumedProteins().add(totalProteins));
		dailyLog.setConsumedFats(dailyLog.getConsumedFats().add(totalFats));
		dailyLog.setConsumedCarbs(dailyLog.getConsumedCarbs().add(totalCarbs));

		nutrientLogRepository.save(dailyLog);
		nutritionUpdatesCounter.increment();

		log.info("Successfully updated daily log for user: {}. Added {} kcal.",
				order.getUser().getId(), totalKcal);
	}

	public double calculateBmr(double weight, double height, int age, String gender) {
		if (age <= 0 || weight <= 0) {
			throw new IllegalArgumentException("Invalid input data");
		}
		double bmr = (10 * weight) + (6.25 * height) - (5 * age);
		return gender.equalsIgnoreCase("male") ? bmr + 5 : bmr - 161;
	}

	public double calculateDci(double bmr, String target) {
		switch (target.toLowerCase()) {
			case "lose": return bmr * 0.85;
			case "gain": return bmr * 1.15;
			case "maintain": return bmr;
			default: throw new IllegalArgumentException("Unknown target");
		}
	}
}