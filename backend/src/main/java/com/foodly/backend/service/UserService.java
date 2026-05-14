package com.foodly.backend.service;

import com.foodly.backend.dto.AuthRequest;
import com.foodly.backend.entity.Role;
import com.foodly.backend.entity.User;
import com.foodly.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.foodly.backend.utils.JwtUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final AuthenticationManager authenticationManager;

	private final JwtUtils jwtUtils;

	@Transactional
	public User registerNewUser(AuthRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			log.warn("LOG-02: Registration failed - email {} already exists", maskEmail(request.getEmail()));
			throw new org.springframework.web.server.ResponseStatusException(
					org.springframework.http.HttpStatus.CONFLICT, "Email already exists"
			);
		}
		User user = User.builder()
			.email(request.getEmail())
			.password(passwordEncoder.encode(request.getPassword()))
			.role(Role.CLIENT)
			.build();

		User savedUser = userRepository.save(user);

		log.info("LOG-03: User {} successfully saved with ID: {}", maskEmail(savedUser.getEmail()), savedUser.getId());

		return savedUser;
	}

	public String authenticateUser(AuthRequest loginRequest) {
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
				loginRequest.getEmail(), loginRequest.getPassword());
		Authentication authentication = authenticationManager.authenticate(authRequest);
		return jwtUtils.generateToken(authentication.getName());
	}

	private String maskEmail(String email) {
		if (email == null || !email.contains("@"))
			return "****";
		return email.replaceAll("(^.{2}).*(@.*$)", "$1***$2");
	}

}
