package com.foodly.backend.controller;

import com.foodly.backend.dto.LoginRequest;
import com.foodly.backend.dto.RegisterRequest;
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
	public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
		log.info("LOG-01: Received registration request for email: {}", maskEmail(request.getEmail()));
		try {
			userService.registerNewUser(request);
			return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
		}
		catch (org.springframework.web.server.ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason()));
		}
		catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
		log.info("LOG-04: Login attempt for user: {}", maskEmail(loginRequest.getEmail()));
		try {
			String token = userService.authenticateUser(loginRequest);
			return ResponseEntity.ok(Map.of("token", token));
		}
		catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password"));
		}
		catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("message", "An unexpected error occurred"));
		}
	}

	private String maskEmail(String email) {
		if (email == null || !email.contains("@"))
			return "****";
		return email.replaceAll("(^.{2}).*(@.*$)", "$1***$2");
	}

}
