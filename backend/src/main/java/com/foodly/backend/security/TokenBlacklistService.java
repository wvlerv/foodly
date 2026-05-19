package com.foodly.backend.security;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

	private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

	public void blacklistToken(String token, long expirationTimeMillis) {
		blacklist.put(token, expirationTimeMillis);
	}

	public boolean isBlacklisted(String token) {
		Long expiration = blacklist.get(token);
		if (expiration == null) {
			return false;
		}

		if (System.currentTimeMillis() > expiration) {
			blacklist.remove(token);
			return false;
		}

		return true;
	}

}