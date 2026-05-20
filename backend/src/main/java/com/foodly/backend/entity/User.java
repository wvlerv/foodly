package com.foodly.backend.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entity class representing a user in the application.
 */
@Entity
@Table(name = "users")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(unique = true, nullable = false)
	private String email;

	@Column(nullable = true)
	private String firstName;

	@Column(nullable = true)
	private String lastName;

	@Column(nullable = true)
	private String username;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	private Role role;

	@Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
	@Builder.Default
	private boolean isDeleted = false;

	@Column(nullable = false)
	@Builder.Default
	private boolean isBanned = false;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private HealthProfile healthProfile;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_favorite_dishes", joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "dish_id"))
	@Builder.Default
	private List<Dish> favoriteDishes = new ArrayList<>();

}
