package com.foodly.backend.controller;

import com.foodly.backend.dto.OrderResponseDto;
import com.foodly.backend.dto.OrderRequestDto;
import com.foodly.backend.service.OrderService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@org.springframework.web.bind.annotation.CrossOrigin(origins = "*")
public class OrderController {

	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

	private final OrderService orderService;

	@GetMapping
	public ResponseEntity<?> getOrders() {
		logger.info("GET /api/orders called");
		try {
			List<OrderResponseDto> orders = orderService.getAllOrders();
			return ResponseEntity.ok(orders);
		}
		catch (Exception e) {
			logger.error("Failed to load orders", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to load orders");
		}
	}

	@GetMapping("/my-orders")
	public ResponseEntity<?> getMyOrders(Authentication authentication) {
		logger.info("GET /api/orders/my-orders called");
		try {
			if (authentication == null || !authentication.isAuthenticated()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
			}

			String email = authentication.getName();
			List<OrderResponseDto> orders = orderService.getOrdersForUser(email);
			return ResponseEntity.ok(orders);
		}
		catch (Exception e) {
			logger.error("Failed to load user orders", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to load user orders");
		}
	}

	@GetMapping("/available")
	@org.springframework.security.access.prepost.PreAuthorize("hasRole('COURIER')")
	public ResponseEntity<?> getAvailableOrders() {
		logger.info("GET /api/orders/available called by courier");
		try {
			List<OrderResponseDto> orders = orderService.getAvailableOrdersForCouriers();
			return ResponseEntity.ok(orders);
		}
		catch (Exception e) {
			logger.error("Failed to load available orders for courier", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to load orders");
		}
	}

	@PutMapping("/{orderId}/deliver")
	@org.springframework.security.access.prepost.PreAuthorize("hasRole('COURIER')")
	public ResponseEntity<?> deliverOrder(@org.springframework.web.bind.annotation.PathVariable java.util.UUID orderId, Authentication authentication) {
		logger.info("PUT /api/orders/{}/deliver called", orderId);
		try {
			if (authentication == null || !authentication.isAuthenticated()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
			}

			String courierEmail = authentication.getName();
			OrderResponseDto updated = orderService.completeOrder(orderId, courierEmail);
			return ResponseEntity.ok(updated);
		}
		catch (Exception e) {
			logger.error("Failed to complete order delivery", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to deliver order: " + e.getMessage());
		}
	}

	@PostMapping
	public ResponseEntity<?> createOrder(@RequestBody OrderRequestDto request, Authentication authentication) {
		logger.info("POST /api/orders called");
		try {
			if (authentication == null || !authentication.isAuthenticated()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
			}

			String email = authentication.getName();
			OrderResponseDto created = orderService.createOrderForUser(email, request);
			return ResponseEntity.status(HttpStatus.CREATED).body(created);
		}
		catch (Exception e) {
			logger.error("Failed to create order", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create order");
		}
	}

}
