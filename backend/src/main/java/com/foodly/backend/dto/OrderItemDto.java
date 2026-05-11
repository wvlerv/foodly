package com.foodly.backend.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto implements Serializable {

	private String name;

	private int quantity;

	private String imageUrl;

	private BigDecimal priceAtPurchase;

}
