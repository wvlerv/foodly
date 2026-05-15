package com.foodly.backend.config;

import com.foodly.backend.entity.Dish;
import com.foodly.backend.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Data initializer for populating the database with healthy menu items. This class runs
 * automatically on application startup and seeds the database with realistic healthy
 * dishes if the dishes table is empty.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MenuDataInitializer implements CommandLineRunner {

	private final DishRepository dishRepository;

	@Override
	public void run(String... args) throws Exception {
		if (dishRepository.count() == 0) {
			log.info("Initializing menu database with default healthy dishes...");
			initializeMenuData();
			log.info("Menu initialization completed successfully!");
		}
		else {
			log.info("Dishes table already contains data. Skipping initialization.");
		}
	}

	private void initializeMenuData() {
		// 1. Chicken Caesar Salad
		Dish chickenCaesar = Dish.builder()
			.name("Chicken Caesar Salad")
			.description(
					"Crispy romaine lettuce with grilled chicken breast, Parmesan cheese, croutons, and creamy Caesar dressing. A classic healthy choice packed with protein.")
			.price(new BigDecimal("12.99"))
			.weight(new BigDecimal("350"))
			.imageUrl("https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&h=500&fit=crop")
			.isAvailable(true)
			.calories(new BigDecimal("380"))
			.proteins(new BigDecimal("35"))
			.fats(new BigDecimal("12"))
			.carbohydrates(new BigDecimal("28"))
			.allergens(new ArrayList<>(Arrays.asList("gluten", "dairy", "nuts")))
			.build();

		// 2. Salmon Quinoa Bowl
		Dish salmonQuinoaBowl = Dish.builder()
			.name("Salmon Quinoa Bowl")
			.description(
					"Grilled salmon fillet served over fluffy quinoa with roasted vegetables, fresh spinach, and a lemon-dill vinaigrette. Rich in omega-3 fatty acids.")
			.price(new BigDecimal("15.99"))
			.weight(new BigDecimal("400"))
			.imageUrl("https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=500&h=500&fit=crop")
			.isAvailable(true)
			.calories(new BigDecimal("520"))
			.proteins(new BigDecimal("42"))
			.fats(new BigDecimal("18"))
			.carbohydrates(new BigDecimal("48"))
			.allergens(new ArrayList<>(Arrays.asList("fish", "nuts")))
			.build();

		// 3. Vegan Avocado Wrap
		Dish veganAvocadoWrap = Dish.builder()
			.name("Vegan Avocado Wrap")
			.description(
					"Whole wheat tortilla filled with creamy avocado, hummus, fresh vegetables (cucumber, tomato, bell peppers), sprouts, and a tahini-lemon drizzle.")
			.price(new BigDecimal("11.99"))
			.weight(new BigDecimal("320"))
			.imageUrl("https://images.unsplash.com/photo-1541519227354-08fa5d50c44d?w=500&h=500&fit=crop")
			.isAvailable(true)
			.calories(new BigDecimal("420"))
			.proteins(new BigDecimal("14"))
			.fats(new BigDecimal("16"))
			.carbohydrates(new BigDecimal("52"))
			.allergens(new ArrayList<>(Arrays.asList("gluten", "sesame", "nuts")))
			.build();

		// 4. Protein Pancakes
		Dish proteinPancakes = Dish.builder()
			.name("Protein Pancakes")
			.description(
					"Fluffy whole wheat pancakes topped with Greek yogurt, fresh berries (blueberries and strawberries), a drizzle of honey, and a sprinkle of granola for crunch.")
			.price(new BigDecimal("10.99"))
			.weight(new BigDecimal("300"))
			.imageUrl("https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=500&h=500&fit=crop")
			.isAvailable(true)
			.calories(new BigDecimal("380"))
			.proteins(new BigDecimal("25"))
			.fats(new BigDecimal("8"))
			.carbohydrates(new BigDecimal("48"))
			.allergens(new ArrayList<>(Arrays.asList("gluten", "dairy", "nuts", "eggs")))
			.build();

		// 5. Beef Steak with Asparagus
		Dish beefSteak = Dish.builder()
			.name("Beef Steak with Asparagus")
			.description(
					"Perfectly grilled lean beef sirloin steak with roasted fresh asparagus, garlic mashed cauliflower, and a rich red wine reduction sauce.")
			.price(new BigDecimal("18.99"))
			.weight(new BigDecimal("450"))
			.imageUrl("https://images.unsplash.com/photo-1600891964092-4316c288032e?w=500&h=500&fit=crop")
			.isAvailable(true)
			.calories(new BigDecimal("580"))
			.proteins(new BigDecimal("52"))
			.fats(new BigDecimal("24"))
			.carbohydrates(new BigDecimal("18"))
			.allergens(new ArrayList<>(Arrays.asList("sulfites")))
			.build();

		// 6. Greek Salad with Feta
		Dish greekSalad = Dish.builder()
			.name("Greek Salad with Feta")
			.description(
					"Fresh garden salad with crisp cucumbers, ripe tomatoes, red onions, Kalamata olives, and creamy feta cheese, dressed with extra virgin olive oil and oregano.")
			.price(new BigDecimal("9.99"))
			.weight(new BigDecimal("280"))
			.imageUrl("https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?w=500&h=500&fit=crop")
			.isAvailable(true)
			.calories(new BigDecimal("320"))
			.proteins(new BigDecimal("12"))
			.fats(new BigDecimal("22"))
			.carbohydrates(new BigDecimal("16"))
			.allergens(new ArrayList<>(Arrays.asList("dairy")))
			.build();

		// Save all dishes to the database
		dishRepository.saveAll(Arrays.asList(chickenCaesar, salmonQuinoaBowl, veganAvocadoWrap, proteinPancakes,
				beefSteak, greekSalad));

		log.info("Successfully seeded {} dishes into the database", 6);
	}

}
