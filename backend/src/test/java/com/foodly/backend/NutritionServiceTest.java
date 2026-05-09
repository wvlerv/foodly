package com.foodly.backend;

import com.foodly.backend.service.NutritionService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NutritionServiceTest {

	private final NutritionService service = new NutritionService();

	@Test
	void testCalculateBMRCorrectly() {
		double result = service.calculateBmr(80, 180, 25, "male");
		assertEquals(1805.0, result); // (10*80) + (6.25*180) - (5*25) + 5
	}

	@Test
	void testBMRInvalidAgeThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			service.calculateBmr(80, 180, -5, "male");
		});
	}

	@Test
	void testCalculateDCIBasedOnTarget() {
		double bmr = 2000.0;

		double loseWeight = service.calculateDci(bmr, "lose");
		double gainWeight = service.calculateDci(bmr, "gain");

		assertAll(() -> assertEquals(1700.0, loseWeight, "Дефіцит для схуднення розраховано невірно"),
				() -> assertEquals(2300.0, gainWeight, "Профіцит для набору розраховано невірно"));
	}

}
