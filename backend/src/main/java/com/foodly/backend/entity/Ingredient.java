package com.foodly.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "calories_per_100g")
    private BigDecimal caloriesPer100g;

    @Column(name = "proteins_per_100g")
    private BigDecimal proteinsPer100g;

    @Column(name = "fats_per_100g")
    private BigDecimal fatsPer100g;

    @Column(name = "carbs_per_100g")
    private BigDecimal carbsPer100g;
}