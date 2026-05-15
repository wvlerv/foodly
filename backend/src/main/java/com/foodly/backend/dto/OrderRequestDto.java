package com.foodly.backend.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDto implements Serializable {

	private String deliveryAddress;

	private String contactPhone;

	private String paymentMethod;

	private List<OrderItemRequestDto> items;

}
