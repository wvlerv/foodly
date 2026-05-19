package com.foodly.backend.config;

import com.foodly.backend.entity.Role;
import com.foodly.backend.entity.User;
import com.foodly.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) throws Exception {
		String adminEmail = "admin@gmail.com";
		if (!userRepository.existsByEmail(adminEmail)) {
			log.info("Creating default admin user...");

			User admin = User.builder()
				.email(adminEmail)
				.firstName("Admin")
				.lastName("Global")
				.username("admin")
				.password(passwordEncoder.encode("adminUlt4#"))
				.role(Role.ADMIN)
				.isBanned(false)
				.build();

			userRepository.save(admin);
			log.info("Admin user successfully created! Email: {}, Password: admin123", adminEmail);
		}
		else {
			log.info("Admin user already exists. Skipping initialization.");
		}
	}

}