package com.foodly.backend.controller;

import com.foodly.backend.dto.OrderResponseDto;
import com.foodly.backend.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
