package com.foodly.backend.dto;

import com.foodly.backend.entity.HealthProfile;
import com.foodly.backend.entity.User;
import java.math.BigDecimal;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Combined profile DTO returned by GET /profile/me.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

	private String email;

	private String firstName;

	private String lastName;

	private String username;

	private Integer age;

	private BigDecimal weight;

	private BigDecimal height;

	private String gender;

	private BigDecimal activityMultiplier;

	private String target;

	private Set<String> allergens;

	private BigDecimal dailyCalorieIntake;

	public static UserProfileDto from(User user) {
		HealthProfile profile = user.getHealthProfile();

		return UserProfileDto.builder()
			.email(user.getEmail())
			.firstName(user.getFirstName())
			.lastName(user.getLastName())
			.username(user.getUsername())
			.age(profile != null ? profile.getAge() : null)
			.weight(profile != null ? profile.getWeight() : null)
			.height(profile != null ? profile.getHeight() : null)
			.gender(profile != null && profile.getGender() != null ? profile.getGender().name() : null)
			.activityMultiplier(profile != null ? profile.getActivityMultiplier() : null)
			.target(profile != null && profile.getTarget() != null ? profile.getTarget().name() : null)
			.allergens(profile != null ? profile.getAllergens() : null)
			.dailyCalorieIntake(profile != null ? profile.getDailyCalorieIntake() : null)
			.build();
	}

}