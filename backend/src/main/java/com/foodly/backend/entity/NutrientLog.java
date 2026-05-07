package com.foodly.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "nutrient_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NutrientLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDate date;

    private BigDecimal consumedCalories;
    private BigDecimal consumedProteins;
    private BigDecimal consumedFats;
    private BigDecimal consumedCarbs;
}