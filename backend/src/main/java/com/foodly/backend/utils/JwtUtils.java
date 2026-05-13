package com.foodly.backend.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import javax.crypto.SecretKey;

@Component
public class JwtUtils {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.expiration}")
	private int jwtExpirationMs;

	public String generateToken(String email) {
		return Jwts.builder()
			.subject(email)
			.issuedAt(new Date())
			.expiration(new Date((new Date()).getTime() + jwtExpirationMs))
			.signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
			.compact();
	}

	public String getEmailFromToken(String token) {
		return Jwts.parser()
			.verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getSubject();
	}

}