package com.foodly.backend.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto implements Serializable {

	private UUID id;

	private LocalDateTime createdAt;

	private String status;

	private BigDecimal totalPrice;

	// Aggregated nutrition for the whole order
	private BigDecimal calories;

	private BigDecimal proteins;

	private BigDecimal fats;

	private BigDecimal carbohydrates;

	// List of ordered items with image and price at purchase
	private List<com.foodly.backend.dto.OrderItemDto> items;

}
