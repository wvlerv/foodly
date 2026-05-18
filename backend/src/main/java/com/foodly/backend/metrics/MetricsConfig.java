package com.foodly.backend.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class MetricsConfig {

	@Bean
	public FilterRegistrationBean<OncePerRequestFilter> httpRequestCountingFilter(MeterRegistry registry) {
		Counter httpRequestsCounter = Counter.builder("http_requests_total")
			.description("Total HTTP requests")
			.register(registry);

		OncePerRequestFilter filter = new OncePerRequestFilter() {
			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					FilterChain filterChain) throws ServletException, IOException {
				try {
					filterChain.doFilter(request, response);
				}
				finally {
					httpRequestsCounter.increment();
				}
			}
		};

		FilterRegistrationBean<OncePerRequestFilter> bean = new FilterRegistrationBean<>(filter);
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		bean.addUrlPatterns("/*");
		return bean;
	}

}
