package com.foodly.backend.repository;

import com.foodly.backend.entity.NutrientLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface NutrientLogRepository extends JpaRepository<NutrientLog, UUID> {

}