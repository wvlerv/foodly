package com.foodly.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the application. Configures CSRF protection and HTTP
 * security.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/**
	 * Configures the security filter chain for HTTP requests.
	 * @param http the HttpSecurity object to configure
	 * @return the configured SecurityFilterChain
	 * @throws Exception if an error occurs during configuration
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable) // Вимикаємо CSRF для розробки
				.authorizeHttpRequests(auth -> auth
						// Дозволяємо доступ до документації Swagger
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						// Дозволяємо доступ до API страв
						.requestMatchers("/api/dishes/**").permitAll()
						// Дозвіл для аналітики
						.requestMatchers("/api/nutrition/**").permitAll()
						// Дозвіл для замовлень
						.requestMatchers("/api/orders/**").permitAll()
						// Усі інші запити потребують авторизації
						.anyRequest().authenticated()
				);

		return http.build();
	}

}
