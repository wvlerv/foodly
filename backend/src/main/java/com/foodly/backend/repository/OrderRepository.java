package com.foodly.backend.repository;

import com.foodly.backend.entity.Order;
import com.foodly.backend.entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

	// Find orders for a specific user ordered by creation time (desc)
	java.util.List<Order> findByUserOrderByCreatedAtDesc(User user);

}
