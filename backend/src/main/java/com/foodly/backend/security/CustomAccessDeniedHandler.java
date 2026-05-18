package com.foodly.backend.security;

import com.foodly.backend.metrics.AuthorizationMetricsService;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	private final AuthorizationMetricsService authorizationMetricsService;

	public CustomAccessDeniedHandler(AuthorizationMetricsService authorizationMetricsService) {
		this.authorizationMetricsService = authorizationMetricsService;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String roles = "anonymous";
		if (auth != null) {
			roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
		}
		authorizationMetricsService.recordDenied(roles, request.getRequestURI());
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		String body = "{\"error\":\"access_denied\"}";
		response.getWriter().write(body);
	}

}
