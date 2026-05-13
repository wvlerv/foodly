package com.foodly.backend.dto;

import com.foodly.backend.entity.Gender;
import com.foodly.backend.entity.WeightTarget;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Set;

@Data
public class HealthProfileDto {
    private int age;
    private BigDecimal weight;
    private BigDecimal height;
    private Gender gender;
    private BigDecimal activityMultiplier;
    private WeightTarget target;
    private Set<String> allergens;
}
