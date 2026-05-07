package com.foodly.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "health_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HealthProfile {
    @Id
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private int age;
    private BigDecimal weight;
    private BigDecimal height;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "activity_multiplier")
    private BigDecimal activityMultiplier; // Напр. 1.2, 1.55

    @Enumerated(EnumType.STRING)
    private WeightTarget target; // LOSE, MAINTAIN, GAIN

    @ElementCollection
    @CollectionTable(name = "user_allergens", joinColumns = @JoinColumn(name = "user_id"))
    private Set<String> allergens;
}
