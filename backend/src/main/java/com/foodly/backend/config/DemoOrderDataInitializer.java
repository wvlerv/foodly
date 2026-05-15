package com.foodly.backend.config;

import com.foodly.backend.entity.Dish;
import com.foodly.backend.entity.Order;
import com.foodly.backend.entity.OrderItem;
import com.foodly.backend.entity.OrderStatus;
import com.foodly.backend.entity.Role;
import com.foodly.backend.entity.User;
import com.foodly.backend.repository.DishRepository;
import com.foodly.backend.repository.OrderRepository;
import com.foodly.backend.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "demo.data", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class DemoOrderDataInitializer implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(DemoOrderDataInitializer.class);

	private final OrderRepository orderRepository;

	private final DishRepository dishRepository;

	private final UserRepository userRepository;

	@Override
	public void run(String... args) {
		initializeIfNeeded();
	}

	@Transactional
	void initializeIfNeeded() {
		User demoUser = userRepository.findByEmail("demo@foodly.local")
			.orElseGet(() -> userRepository
				.save(User.builder().email("demo@foodly.local").password("demo").role(Role.CLIENT).build()));

		// clear all old orders so we can re-seed with updated list
		orderRepository.deleteAll();
		logger.info("Cleared all orders for refresh");

		List<Dish> dishes = new ArrayList<>(dishRepository.findAll());
		if (dishes.size() < 2) {
			dishes.add(dishRepository.save(Dish.builder()
				.name("Chicken Bowl")
				.description("Grilled chicken with quinoa and vegetables")
				.price(new BigDecimal("8.90"))
				.imageUrl("https://images.unsplash.com/photo-1512621776951-a57141f2eefd")
				.calories(new BigDecimal("420"))
				.proteins(new BigDecimal("32"))
				.fats(new BigDecimal("14"))
				.carbohydrates(new BigDecimal("38"))
				.isAvailable(true)
				.allergens(List.of("soy"))
				.build()));

			dishes.add(dishRepository.save(Dish.builder()
				.name("Salmon Power Plate")
				.description("Baked salmon, brown rice and greens")
				.price(new BigDecimal("11.50"))
				.imageUrl("https://images.unsplash.com/photo-1467003909585-2f8a72700288")
				.calories(new BigDecimal("530"))
				.proteins(new BigDecimal("40"))
				.fats(new BigDecimal("20"))
				.carbohydrates(new BigDecimal("44"))
				.isAvailable(true)
				.allergens(List.of("fish"))
				.build()));
		}

		Dish firstDish = dishes.get(0);
		Dish secondDish = dishes.get(1);

		// create 5 demo orders - one for each status with varied items
		List<Order> demoOrders = new ArrayList<>();

		// CREATED: 3x firstDish
		Order o1 = Order.builder()
			.user(demoUser)
			.status(OrderStatus.CREATED)
			.totalPrice(firstDish.getPrice().multiply(BigDecimal.valueOf(3)))
			.createdAt(LocalDateTime.now().minusDays(2))
			.build();
		o1.addItem(OrderItem.builder().dish(firstDish).quantity(3).priceAtPurchase(firstDish.getPrice()).build());
		demoOrders.add(o1);

		// PAID: 1x secondDish + 1x firstDish (mixed)
		Order o2 = Order.builder()
			.user(demoUser)
			.status(OrderStatus.PAID)
			.totalPrice(secondDish.getPrice().add(firstDish.getPrice()))
			.createdAt(LocalDateTime.now().minusHours(20))
			.build();
		o2.addItem(OrderItem.builder().dish(secondDish).quantity(1).priceAtPurchase(secondDish.getPrice()).build());
		o2.addItem(OrderItem.builder().dish(firstDish).quantity(1).priceAtPurchase(firstDish.getPrice()).build());
		demoOrders.add(o2);

		// PREPARING: 2x secondDish
		Order o3 = Order.builder()
			.user(demoUser)
			.status(OrderStatus.PREPARING)
			.totalPrice(secondDish.getPrice().multiply(BigDecimal.valueOf(2)))
			.createdAt(LocalDateTime.now().minusHours(4))
			.build();
		o3.addItem(OrderItem.builder().dish(secondDish).quantity(2).priceAtPurchase(secondDish.getPrice()).build());
		demoOrders.add(o3);

		// DELIVERED: 2x firstDish + 1x secondDish
		Order o4 = Order.builder()
			.user(demoUser)
			.status(OrderStatus.DELIVERED)
			.totalPrice(firstDish.getPrice().multiply(BigDecimal.valueOf(2)).add(secondDish.getPrice()))
			.createdAt(LocalDateTime.now().minusDays(4))
			.build();
		o4.addItem(OrderItem.builder().dish(firstDish).quantity(2).priceAtPurchase(firstDish.getPrice()).build());
		o4.addItem(OrderItem.builder().dish(secondDish).quantity(1).priceAtPurchase(secondDish.getPrice()).build());
		demoOrders.add(o4);

		// CANCELLED: 1x firstDish
		Order o5 = Order.builder()
			.user(demoUser)
			.status(OrderStatus.CANCELLED)
			.totalPrice(firstDish.getPrice())
			.createdAt(LocalDateTime.now().minusHours(1))
			.build();
		o5.addItem(OrderItem.builder().dish(firstDish).quantity(1).priceAtPurchase(firstDish.getPrice()).build());
		demoOrders.add(o5);

		demoOrders.forEach(orderRepository::save);

		logger.info("Seeded {} demo orders for demo@foodly.local", demoOrders.size());
	}

}
