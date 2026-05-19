package com.foodly.backend.repository;

import com.foodly.backend.entity.Order;
import com.foodly.backend.entity.OrderStatus;
import com.foodly.backend.entity.User;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

	// Find orders for a specific user ordered by creation time (desc)
	java.util.List<Order> findByUserOrderByCreatedAtDesc(User user);

	// Знайти всі замовлення певного кур'єра за часом створення
	List<Order> findByCourierOrderByCreatedAtDesc(User courier);

	@EntityGraph(attributePaths = { "user" })
	// Знайти всі замовлення з певним статусом, які ще не закріплені за жодним кур'єром
	List<Order> findByStatusAndCourierIsNullOrderByCreatedAtDesc(OrderStatus status);

}
