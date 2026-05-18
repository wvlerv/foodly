package com.foodly.backend.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ActiveSessionService {

	private final MeterRegistry registry;

	private final ConcurrentHashMap<String, AtomicInteger> sessionsByRole = new ConcurrentHashMap<>();

	public ActiveSessionService(MeterRegistry registry) {
		this.registry = registry;
        getOrCreateCounter("CLIENT");
        getOrCreateCounter("ADMIN");
	}

	public void increment(String role) {
		getOrCreateCounter(role).incrementAndGet();
	}

	public void decrement(String role) {
		AtomicInteger ai = sessionsByRole.get(role);
		if (ai != null) {
			ai.decrementAndGet();
		}
	}

	private AtomicInteger getOrCreateCounter(String role) {
		return sessionsByRole.computeIfAbsent(role, r -> {
			AtomicInteger ai = new AtomicInteger(0);
			registry.gauge("active_user_sessions", Tags.of("role", r), ai);
			return ai;
		});
	}

	public int get(String role) {
		AtomicInteger ai = sessionsByRole.get(role);
		return ai != null ? ai.get() : 0;
	}

}
