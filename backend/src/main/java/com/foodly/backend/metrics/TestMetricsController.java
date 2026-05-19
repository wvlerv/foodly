package com.foodly.backend.metrics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/actuator-test")
public class TestMetricsController {

	private final DatabaseMetricsService databaseMetricsService;

	private final ActiveSessionService activeSessionService;

	public TestMetricsController(DatabaseMetricsService databaseMetricsService,
			ActiveSessionService activeSessionService) {
		this.databaseMetricsService = databaseMetricsService;
		this.activeSessionService = activeSessionService;
	}

	@PostMapping("/db")
	public ResponseEntity<String> incrementDb() {
		databaseMetricsService.incrementQueryCount();
		return ResponseEntity.ok("db incremented");
	}

	@PostMapping("/session/increment")
	public ResponseEntity<String> incrementSession(@RequestParam(defaultValue = "CLIENT") String role) {
		activeSessionService.increment(role);
		return ResponseEntity.ok("session incremented");
	}

	@PostMapping("/session/decrement")
	public ResponseEntity<String> decrementSession(@RequestParam(defaultValue = "CLIENT") String role) {
		activeSessionService.decrement(role);
		return ResponseEntity.ok("session decremented");
	}

	@GetMapping("/admin-only")
	public ResponseEntity<String> adminOnly() {
		return ResponseEntity.ok("admin area");
	}

}
