package com.foodly.backend.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationMetricsService {

	private final MeterRegistry registry;

	public AuthorizationMetricsService(MeterRegistry registry) {
		this.registry = registry;
	}

	public void recordDenied(String role, String path) {
		Tags tags = Tags.of("role", role == null ? "anonymous" : role, "path", path == null ? "unknown" : path);
		registry.counter("authorization_denied_total", tags).increment();
	}

}
