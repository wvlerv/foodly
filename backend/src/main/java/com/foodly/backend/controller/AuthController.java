package com.foodly.backend.controller;

import com.foodly.backend.dto.AuthRequest;
import com.foodly.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final UserService userService;

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody AuthRequest request) {
		log.info("LOG-01: Received registration request for email: {}", maskEmail(request.getEmail()));
		try {
			userService.registerNewUser(request);
			return ResponseEntity.ok("User registered successfully as CLIENT!");
		}
		catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody AuthRequest loginRequest) {
		log.info("LOG-04: Login attempt for user: {}", maskEmail(loginRequest.getEmail()));

		try {
			String token = userService.authenticateUser(loginRequest);

			log.info("LOG-05: User {} logged in successfully.", maskEmail(loginRequest.getEmail()));
			return ResponseEntity.ok(Map.of("token", token));

		}
		catch (BadCredentialsException e) {
			log.warn("LOG-08: Login failed - invalid credentials for user: {}", maskEmail(loginRequest.getEmail()));
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
		}
		catch (Exception e) {
			log.error("LOG-09: Unexpected error during login: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
		}
	}

	private String maskEmail(String email) {
		if (email == null || !email.contains("@"))
			return "****";
		return email.replaceAll("(^.{2}).*(@.*$)", "$1***$2");
	}

}
