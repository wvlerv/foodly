package com.foodly.backend.service;

import com.foodly.backend.dto.LoginRequest;
import com.foodly.backend.dto.RegisterRequest;
import com.foodly.backend.entity.Role;
import com.foodly.backend.entity.User;
import com.foodly.backend.dto.ChangePasswordRequest;
import com.foodly.backend.repository.UserRepository;
import com.foodly.backend.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.foodly.backend.utils.JwtUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final AuthenticationManager authenticationManager;

	private final TokenBlacklistService blacklistService;

	private final JwtUtils jwtUtils;

	@Transactional
	public User registerNewUser(RegisterRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			log.warn("LOG-02: Registration failed - email {} already exists", maskEmail(request.getEmail()));
			throw new org.springframework.web.server.ResponseStatusException(
					org.springframework.http.HttpStatus.CONFLICT, "Email already exists");
		}
		User user = User.builder()
			.email(request.getEmail())
			.firstName(request.getFirstName())
			.lastName(request.getLastName())
			.username(request.getUsername())
			.password(passwordEncoder.encode(request.getPassword()))
			.role(Role.CLIENT)
			.build();

		User savedUser = userRepository.save(user);

		log.info("LOG-03: User {} successfully saved with ID: {}", maskEmail(savedUser.getEmail()), savedUser.getId());

		return savedUser;
	}

	public Map<String, String> authenticateUser(LoginRequest loginRequest) {
		User user = userRepository.findByEmail(loginRequest.getEmail())
			.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
		if (user.isBanned()) {
			log.warn("LOG-05: Login rejected - User {} is banned", maskEmail(user.getEmail()));
			throw new LockedException("Your account has been banned. Please contact support.");
		}

		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
				loginRequest.getEmail(), loginRequest.getPassword());
		Authentication authentication = authenticationManager.authenticate(authRequest);

		String token = jwtUtils.generateToken(authentication.getName());

		return Map.of("token", token, "role", user.getRole().name());
	}

	public void logoutUser(String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new IllegalArgumentException("Authorization header missing or invalid");
		}

		String token = authHeader.substring(7).trim();

		try {
			long expirationTime = jwtUtils.getExpirationDateFromToken(token).getTime();

			blacklistService.blacklistToken(token, expirationTime);

			log.info("LOG-11: Token successfully added to blacklist for logout.");
		}
		catch (Exception e) {
			log.error("LOG-12: Error processing token during logout: {}", e.getMessage());
			throw new RuntimeException("Failed to process token during logout", e);
		}
	}

	@Transactional
	public void changePassword(String email, ChangePasswordRequest request) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
			log.warn("Password change failed for {} - incorrect old password", maskEmail(email));
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect old password");
		}

		if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be at least 6 characters long");
		}

		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);
		log.info("Password successfully updated for user ID: {}", user.getId());
	}

	@Transactional
	public void softDeleteUser(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		user.setDeleted(true);
		user.setEmail("deleted_" + user.getId() + "_" + user.getEmail());

		userRepository.save(user);
		log.info("User ID: {} has been soft-deleted and anonymized", user.getId());
	}

	private String maskEmail(String email) {
		if (email == null || !email.contains("@"))
			return "****";
		return email.replaceAll("(^.{2}).*(@.*$)", "$1***$2");
	}

}
