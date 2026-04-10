package com.foodly.backend.entity;

import jakarta.persistence.*;
import lombok.*;
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
    private double caloriesPer100g;

    @Column(name = "proteins_per_100g")
    private double proteinsPer100g;

    @Column(name = "fats_per_100g")
    private double fatsPer100g;

    @Column(name = "carbs_per_100g")
    private double carbsPer100g;
}