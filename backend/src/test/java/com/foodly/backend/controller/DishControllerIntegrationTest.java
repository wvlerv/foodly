package com.foodly.backend.controller;

import com.foodly.backend.entity.Dish;
import com.foodly.backend.repository.DishRepository;
import com.foodly.backend.service.DishService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Simplified Integration Test for DishController using Mockito (Lab 3). Tests the "Fit my
 * day" filter functionality without requiring a real database.
 */
@ExtendWith(MockitoExtension.class)
class DishControllerIntegrationTest {

	@Mock
	private DishRepository dishRepository;

	private DishService dishService;

	private DishController dishController;

	private MockMvc mockMvc;

	private Dish dish1;

	private Dish dish2;

	private Dish dish3;

	@BeforeEach
	void setUp() {
		// Initialize DishService with mocked repository
		dishService = new DishService(dishRepository, new SimpleMeterRegistry());
		dishController = new DishController(dishService);

		// Set up MockMvc with the controller
		mockMvc = MockMvcBuilders.standaloneSetup(dishController).build();

		// Create test dishes with known calorie values
		dish1 = Dish.builder()
			.id(UUID.randomUUID())
			.name("Grilled Chicken")
			.description("Delicious grilled chicken with herbs")
			.price(BigDecimal.valueOf(12.99))
			.weight(BigDecimal.valueOf(200))
			.imageUrl("http://example.com/chicken.jpg")
			.isAvailable(true)
			.calories(BigDecimal.valueOf(350))
			.proteins(BigDecimal.valueOf(45))
			.fats(BigDecimal.valueOf(10))
			.carbohydrates(BigDecimal.valueOf(0))
			.allergens(Arrays.asList("Gluten"))
			.build();

		dish2 = Dish.builder()
			.id(UUID.randomUUID())
			.name("Caesar Salad")
			.description("Fresh Caesar salad with homemade dressing")
			.price(BigDecimal.valueOf(9.99))
			.weight(BigDecimal.valueOf(250))
			.imageUrl("http://example.com/salad.jpg")
			.isAvailable(true)
			.calories(BigDecimal.valueOf(450))
			.proteins(BigDecimal.valueOf(20))
			.fats(BigDecimal.valueOf(25))
			.carbohydrates(BigDecimal.valueOf(30))
			.allergens(Arrays.asList("Dairy", "Eggs"))
			.build();

		dish3 = Dish.builder()
			.id(UUID.randomUUID())
			.name("Chocolate Cake")
			.description("Rich chocolate cake")
			.price(BigDecimal.valueOf(7.99))
			.weight(BigDecimal.valueOf(150))
			.imageUrl("http://example.com/cake.jpg")
			.isAvailable(true)
			.calories(BigDecimal.valueOf(600))
			.proteins(BigDecimal.valueOf(8))
			.fats(BigDecimal.valueOf(30))
			.carbohydrates(BigDecimal.valueOf(75))
			.allergens(Arrays.asList("Dairy", "Nuts"))
			.build();
	}

	/**
     * Test: Verify that passing remainingKcal=500 returns only dishes under 500 kcal.
     * Expected: Grilled Chicken (350 kcal) and Caesar Salad (450 kcal) should be returned.
     * Not returned: Chocolate Cake (600 kcal) exceeds 500 kcal.
     */
    @Test
    void testFitMyDayFilterWithRemaining500Kcal() throws Exception {
        // Mock repository to return all available dishes
        when(dishRepository.findAllAvailable()).thenReturn(Arrays.asList(dish1, dish2, dish3));

        mockMvc.perform(get("/api/dishes")
                .param("remainingKcal", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Grilled Chicken", "Caesar Salad")))
                .andExpect(jsonPath("$[*].calories", containsInAnyOrder(350, 450)));
    }

	/**
     * Test: Verify that sorting by calories works correctly within the "Fit my day" filter.
     * Expected: Results should be ordered by calories in ascending order.
     */
    @Test
    void testFitMyDayFilterWithCalorieSorting() throws Exception {
        // Mock repository for sorted results
        when(dishRepository.findAllAvailableSorted("calories"))
                .thenReturn(Arrays.asList(dish1, dish2, dish3));

        mockMvc.perform(get("/api/dishes")
                .param("remainingKcal", "500")
                .param("sortBy", "calories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Grilled Chicken")))
                .andExpect(jsonPath("$[0].calories", is(350)))
                .andExpect(jsonPath("$[1].name", is("Caesar Salad")))
                .andExpect(jsonPath("$[1].calories", is(450)));
    }

	/**
	 * Test: Verify that invalid remainingKcal returns empty list. Expected: Passing
	 * remainingKcal=0 or negative values should return an empty array.
	 */
	@Test
	void testFitMyDayFilterWithInvalidRemainingKcal() throws Exception {
		mockMvc.perform(get("/api/dishes").param("remainingKcal", "0"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));
	}

	/**
     * Test: Verify that retrieving all dishes without filter works correctly.
     * Expected: All three dishes should be returned.
     */
    @Test
    void testGetAllDishesWithoutFilter() throws Exception {
        // Mock repository to return all available dishes
        when(dishRepository.findAllAvailable()).thenReturn(Arrays.asList(dish1, dish2, dish3));

        mockMvc.perform(get("/api/dishes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Grilled Chicken", "Caesar Salad", "Chocolate Cake")));
    }

	/**
     * Test: Verify that allergen data is included in the response for warnings (Story 2.2).
     * Expected: Each dish should have an allergens list.
     */
    @Test
    void testDishResponseIncludesAllergens() throws Exception {
        // Mock repository to return all available dishes
        when(dishRepository.findAllAvailable()).thenReturn(Arrays.asList(dish1, dish2, dish3));

        mockMvc.perform(get("/api/dishes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].allergens", notNullValue()))
                .andExpect(jsonPath("$[0].allergens", hasSize(greaterThanOrEqualTo(1))));
    }

}
