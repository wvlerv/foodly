package com.foodly.backend.repository;

import java.util.UUID;
import com.foodly.backend.entity.HealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthProfileRepository extends JpaRepository<HealthProfile, UUID> {
}
