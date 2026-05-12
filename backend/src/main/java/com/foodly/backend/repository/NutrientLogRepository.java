package com.foodly.backend.repository;

import com.foodly.backend.entity.NutrientLog;
import com.foodly.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface NutrientLogRepository extends JpaRepository<NutrientLog, UUID> {
    Optional<NutrientLog> findByUserAndDate(User user, LocalDate date);
}