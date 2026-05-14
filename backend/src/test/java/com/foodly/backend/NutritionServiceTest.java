package com.foodly.backend;

import com.foodly.backend.service.NutritionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class) // 1. Включаем поддержку Mockito
class NutritionServiceTest {

	@InjectMocks // 2. Просим Mockito самостоятельно создать этот сервис
	private NutritionService service;

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

		assertAll(() -> assertEquals(1700.0, loseWeight, "Deficit for weight loss calculated incorrectly"),
				() -> assertEquals(2300.0, gainWeight, "Surplus for weight gain calculated incorrectly"));
	}

}