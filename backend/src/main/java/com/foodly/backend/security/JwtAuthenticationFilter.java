package com.foodly.backend.security;

import com.foodly.backend.entity.User;
import com.foodly.backend.repository.UserRepository;
import com.foodly.backend.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtils jwtUtils;
	private final TokenBlacklistService blacklistService;
	private final UserRepository userRepository;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
	                                @NonNull FilterChain filterChain) throws ServletException, IOException {
		try {
			String jwt = parseJwt(request);
			if (jwt != null && !blacklistService.isBlacklisted(jwt) && jwtUtils.validateToken(jwt)) {
				String email = jwtUtils.getEmailFromToken(jwt);

				Optional<User> userOptional = userRepository.findByEmail(email);

				if (userOptional.isPresent()) {
					User user = userOptional.get();

					if (user.isBanned()) {
						log.warn("LOG-09: Access denied - User {} is banned", email);
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						response.setContentType("application/json");
						response.getWriter().write("{\"message\": \"Your account has been banned.\"}");
						return;
					}

					log.debug("LOG-06: JWT validated for user: {}", email);
					String roleWithPrefix = "ROLE_" + user.getRole().name();
					List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleWithPrefix));
					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							email, null, authorities);

					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
			else if (jwt != null && blacklistService.isBlacklisted(jwt)) {
				log.warn("LOG-10: Access denied - token is blacklisted");
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
		}
		catch (Exception e) {
			log.error("LOG-07: Authentication error: {}", e.getMessage());
		}
		filterChain.doFilter(request, response);
	}

	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");
		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			String jwt = headerAuth.substring(7).trim();
			if (jwt.chars().filter(ch -> ch == '.').count() != 2) {
				log.warn("LOG-08: Malformed JWT received: {}", jwt);
				return null;
			}
			return jwt;
		}
		return null;
	}
}