package com.foodly.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class representing a user's health profile including dietary preferences and
 * personal metrics.
 */
@Entity
@Table(name = "health_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthProfile {

	@Id
	private UUID id;

	@OneToOne
	@MapsId
	@JoinColumn(name = "user_id")
	@JsonIgnore
	private User user;

	private int age;

	private BigDecimal weight;

	private BigDecimal height;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	@Column(name = "activity_multiplier")
	private BigDecimal activityMultiplier;

	@Enumerated(EnumType.STRING)
	private WeightTarget target;

	@ElementCollection
	@CollectionTable(name = "user_allergens", joinColumns = @JoinColumn(name = "user_id"))
	private Set<String> allergens;

	@Column(name = "daily_calorie_intake")
	private BigDecimal dailyCalorieIntake;

}
