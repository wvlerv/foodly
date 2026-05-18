package com.foodly.backend.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class DatabaseMetricsService {

	private final Counter dbQueries;

	public DatabaseMetricsService(MeterRegistry registry) {
		this.dbQueries = Counter.builder("db_queries_total").description("Total DB queries").register(registry);
	}

	public void incrementQueryCount() {
		dbQueries.increment();
	}

}
