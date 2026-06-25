package com.nilesh.knowledgebase.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

	@NotBlank(message = "JWT secret is required")
	@Size(min = 32, message = "JWT secret must be at least 32 characters")
	private String secret;
	@NotNull
	private Duration expiration = Duration.ofHours(2);
	@NotBlank(message = "JWT issuer is required")
	private String issuer = "knowledge-base";

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public Duration getExpiration() {
		return expiration;
	}

	public void setExpiration(Duration expiration) {
		this.expiration = expiration;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
}
