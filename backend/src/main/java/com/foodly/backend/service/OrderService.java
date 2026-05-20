package com.foodly.backend.service;

import com.foodly.backend.dto.OrderResponseDto;
import com.foodly.backend.dto.OrderRequestDto;
import com.foodly.backend.dto.OrderItemRequestDto;
import com.foodly.backend.entity.Dish;
import com.foodly.backend.entity.Order;
import com.foodly.backend.entity.OrderItem;
import com.foodly.backend.entity.User;
import com.foodly.backend.repository.OrderRepository;
import com.foodly.backend.repository.UserRepository;
import com.foodly.backend.repository.DishRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

	private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

	private final OrderRepository orderRepository;

	private final UserRepository userRepository;

	private final DishRepository dishRepository;

	@Transactional(readOnly = true)
	public List<OrderResponseDto> getAllOrders() {
		List<Order> orders = orderRepository.findAll();
		logger.info("Loaded {} orders from repository", orders.size());

		return orders.stream().map(this::toDto).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<OrderResponseDto> getOrdersForUser(String email) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("User not found: " + email));

		List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
		logger.info("Loaded {} orders for user {}", orders.size(), email);

		return orders.stream().map(this::toDto).collect(Collectors.toList());
	}

	@Transactional
	public OrderResponseDto createOrderForUser(String email, OrderRequestDto request) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("User not found: " + email));

		Order order = Order.builder()
			.user(user)
			.status(com.foodly.backend.entity.OrderStatus.CREATED)
			.deliveryAddress(request.getDeliveryAddress())
			.contactPhone(request.getContactPhone())
			.paymentMethod(request.getPaymentMethod())
			.createdAt(java.time.LocalDateTime.now())
			.items(new java.util.ArrayList<>())
			.build();

		java.math.BigDecimal total = java.math.BigDecimal.ZERO;

		if (request.getItems() != null) {
			for (OrderItemRequestDto itemReq : request.getItems()) {
				Dish dish = dishRepository.findById(itemReq.getDishId())
					.orElseThrow(() -> new RuntimeException("Dish not found: " + itemReq.getDishId()));

				OrderItem item = OrderItem.builder()
					.dish(dish)
					.quantity(itemReq.getQuantity())
					.priceAtPurchase(dish.getPrice())
					.build();

				order.addItem(item);
				total = total.add(item.getPriceAtPurchase().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
			}
		}

		order.setTotalPrice(total);

		Order saved = orderRepository.save(order);
		logger.info("Created order {} for user {}", saved.getId(), email);
		return toDto(saved);
	}

	@Transactional(readOnly = true)
	public List<OrderResponseDto> getAvailableOrdersForCouriers() {
		// Кур'єр бачить абсолютно всі замовлення, які щойно створені клієнтами
		List<Order> availableOrders = orderRepository
			.findByStatusAndCourierIsNullOrderByCreatedAtDesc(com.foodly.backend.entity.OrderStatus.CREATED);

		logger.info("Loaded {} new orders for the courier", availableOrders.size());
		return availableOrders.stream().map(this::toDto).collect(Collectors.toList());
	}

	@Transactional
	public OrderResponseDto completeOrder(UUID orderId, String courierEmail) {
		// Шукаємо замовлення в базі
		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

		// Шукаємо нашого єдиного кур'єра
		User courier = userRepository.findByEmail(courierEmail)
			.orElseThrow(() -> new RuntimeException("Courier not found: " + courierEmail));

		// Прямо міняємо статус на DELIVERED
		order.setCourier(courier);
		order.setStatus(com.foodly.backend.entity.OrderStatus.DELIVERED);

		Order updatedOrder = orderRepository.save(order);
		logger.info("Order {} has been successfully DELIVERED by courier {}", orderId, courierEmail);

		return toDto(updatedOrder);
	}

	private OrderResponseDto toDto(Order order) {
		BigDecimal calories = BigDecimal.ZERO;
		BigDecimal proteins = BigDecimal.ZERO;
		BigDecimal fats = BigDecimal.ZERO;
		BigDecimal carbs = BigDecimal.ZERO;

		List<com.foodly.backend.dto.OrderItemDto> itemSummaries = new ArrayList<>();

		for (OrderItem item : order.getItems()) {
			Dish dish = item.getDish();
			int qty = item.getQuantity();
			BigDecimal q = BigDecimal.valueOf(qty);

			if (dish != null) {
				BigDecimal dcal = safe(dish.getCalories());
				BigDecimal dprot = safe(dish.getProteins());
				BigDecimal dfat = safe(dish.getFats());
				BigDecimal dcarb = safe(dish.getCarbohydrates());

				calories = calories.add(dcal.multiply(q));
				proteins = proteins.add(dprot.multiply(q));
				fats = fats.add(dfat.multiply(q));
				carbs = carbs.add(dcarb.multiply(q));

				itemSummaries.add(com.foodly.backend.dto.OrderItemDto.builder()
					.name(dish.getName())
					.quantity(qty)
					.imageUrl(dish.getImageUrl())
					.priceAtPurchase(item.getPriceAtPurchase())
					.build());
			}
			else {
				itemSummaries.add(com.foodly.backend.dto.OrderItemDto.builder()
					.name("(deleted dish)")
					.quantity(qty)
					.imageUrl(null)
					.priceAtPurchase(item.getPriceAtPurchase())
					.build());
			}
		}

		// Normalize scale similar to DishService usage
		calories = calories.setScale(2, RoundingMode.HALF_UP);
		proteins = proteins.setScale(2, RoundingMode.HALF_UP);
		fats = fats.setScale(2, RoundingMode.HALF_UP);
		carbs = carbs.setScale(2, RoundingMode.HALF_UP);

		return OrderResponseDto.builder()
			.id(order.getId())
			.createdAt(order.getCreatedAt())
			.deliveryAddress(order.getDeliveryAddress())
			.contactPhone(order.getContactPhone())
			.paymentMethod(order.getPaymentMethod())
			.status(order.getStatus() != null ? order.getStatus().name() : null)
			.totalPrice(order.getTotalPrice())
			.clientName(order.getUser() != null ? order.getUser().getFirstName() : "Deleted User")
			.calories(calories)
			.proteins(proteins)
			.fats(fats)
			.carbohydrates(carbs)
			.items(itemSummaries)
			.build();
	}

	private BigDecimal safe(BigDecimal v) {
		return v == null ? BigDecimal.ZERO : v;
	}

}
