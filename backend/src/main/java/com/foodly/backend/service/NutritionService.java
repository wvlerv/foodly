package com.foodly.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// Імпорти наших репозиторіїв та сутностей
import com.foodly.backend.entity.HealthProfile;
import com.foodly.backend.entity.Gender;
import com.foodly.backend.entity.WeightTarget;
import com.foodly.backend.entity.User;
import com.foodly.backend.entity.Order;
import com.foodly.backend.entity.OrderItem;
import com.foodly.backend.entity.OrderStatus;
import com.foodly.backend.repository.UserRepository;
import com.foodly.backend.repository.OrderRepository;
import com.foodly.backend.dto.NutritionAnalyticsResponse;

/**
 * Service for calculating nutritional metrics like BMR (Basal Metabolic Rate) and DCI
 * (Daily Caloric Intake).
 */
@Service
public class NutritionService {

	private final UserRepository userRepository;

	private final OrderRepository orderRepository;

	public NutritionService(UserRepository userRepository, OrderRepository orderRepository) {
		this.userRepository = userRepository;
		this.orderRepository = orderRepository;
	}

	/**
	 * Calculates the Basal Metabolic Rate (BMR) using the Mifflin-St Jeor equation.
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

	@Transactional(readOnly = true)
	public NutritionAnalyticsResponse getUserAnalytics(String email) {
		// 1. Ищем пользователя и его медицинский профиль
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("User not found: " + email));

		// 2. Считаем персональную норму калорий (DCI)
		BigDecimal dailyGoal = BigDecimal.valueOf(2000); // Дефолт, если анкеты здоровья
															// нет
		if (user.getHealthProfile() != null) {
			dailyGoal = calculateFullDci(user.getHealthProfile());
		}

		// 3. Извлекаем ВСЕ заказы пользователя
		List<Order> userOrders = orderRepository.findByUserOrderByCreatedAtDesc(user);

		// 4. Группируем калории по датам (СТРОГО для доставленных заказов)
		Map<String, Integer> caloriesByDate = new TreeMap<>();
		boolean hasDeliveredOrders = false; // Флаг: есть ли хотя бы один доставленный
											// заказ

		for (Order order : userOrders) {
			// КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ: Берем только те заказы, которые реально доставлены
			// клиенту
			if (order.getStatus() == OrderStatus.DELIVERED) {
				hasDeliveredOrders = true;

				String dateKey = order.getCreatedAt().toLocalDate().toString(); // Формат
																				// "YYYY-MM-DD"

				int orderCalories = 0;
				for (OrderItem item : order.getItems()) {
					if (item.getDish() != null && item.getDish().getCalories() != null) {
						orderCalories += item.getDish().getCalories().intValue() * item.getQuantity();
					}
				}

				// Суммируем калории за этот день
				caloriesByDate.put(dateKey, caloriesByDate.getOrDefault(dateKey, 0) + orderCalories);
			}
		}

		// 5. Формируем список DTO для фронтенда
		List<NutritionAnalyticsResponse.CalorieLogDto> logDtos = new ArrayList<>();

		// Если доставленных заказов нет, оставляем список logDtos ПУСТЫМ.
		// Фронтенд увидит, что logs.length === 0, и безопасно переключится на
		// MOCK_FALLBACK_DATA
		if (hasDeliveredOrders) {
			for (Map.Entry<String, Integer> entry : caloriesByDate.entrySet()) {
				logDtos.add(NutritionAnalyticsResponse.CalorieLogDto.builder()
					.date(entry.getKey())
					.consumedCalories(entry.getValue())
					.build());
			}
		}

		return NutritionAnalyticsResponse.builder().dailyGoal(dailyGoal).logs(logDtos).build();
	}

	public BigDecimal calculateFullDci(HealthProfile profile) {
		BigDecimal bmr = calculateBmrFromProfile(profile);
		BigDecimal tdee = bmr.multiply(profile.getActivityMultiplier());
		BigDecimal result = tdee;
		if (profile.getTarget() == WeightTarget.LOSE) {
			result = tdee.multiply(new BigDecimal("0.85")); // -15%
		}
		else if (profile.getTarget() == WeightTarget.GAIN) {
			result = tdee.multiply(new BigDecimal("1.15")); // +15%
		}
		return result.setScale(0, java.math.RoundingMode.HALF_UP);
	}

	private BigDecimal calculateBmrFromProfile(HealthProfile profile) {
		BigDecimal weightPart = new BigDecimal("10").multiply(profile.getWeight());
		BigDecimal heightPart = new BigDecimal("6.25").multiply(profile.getHeight());
		BigDecimal agePart = new BigDecimal("5").multiply(new BigDecimal(profile.getAge()));

		BigDecimal bmr = weightPart.add(heightPart).subtract(agePart);

		if (profile.getGender() == Gender.MALE) {
			return bmr.add(new BigDecimal("5"));
		}
		else {
			return bmr.subtract(new BigDecimal("161"));
		}
	}

}