package com.nilesh.knowledgebase.security;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import com.nilesh.knowledgebase.config.JwtProperties;
import com.nilesh.knowledgebase.entity.User;
import com.nilesh.knowledgebase.exception.JwtAuthenticationException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final JwtProperties jwtProperties;
	private final Clock clock = Clock.systemUTC();
	private final SecretKey secretKey;

	public JwtService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(User user) {
		Instant now = clock.instant();
		Instant expiresAt = now.plus(jwtProperties.getExpiration());
		return Jwts.builder()
			.issuer(jwtProperties.getIssuer())
			.subject(user.getEmail())
			.claim("userId", user.getId().toString())
			.claim("role", user.getRole().name())
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiresAt))
			.signWith(secretKey)
			.compact();
	}

	public String extractUsername(String token) {
		return parseClaims(token).getSubject();
	}

	public UUID extractUserId(String token) {
		return UUID.fromString(parseClaims(token).get("userId", String.class));
	}

	public boolean isTokenValid(String token) {
		try {
			Claims claims = parseClaims(token);
			return claims.getExpiration().toInstant().isAfter(clock.instant());
		} catch (JwtException | IllegalArgumentException exception) {
			return false;
		}
	}

	private Claims parseClaims(String token) {
		try {
			return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (JwtException | IllegalArgumentException exception) {
			throw new JwtAuthenticationException("Invalid JWT token");
		}
	}
}