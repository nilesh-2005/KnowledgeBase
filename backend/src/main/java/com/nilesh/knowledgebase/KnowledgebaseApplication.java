package com.nilesh.knowledgebase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableJpaAuditing
public class KnowledgebaseApplication {

	public static void main(String[] args) {
		loadEnv();
		SpringApplication.run(KnowledgebaseApplication.class, args);
	}

	private static void loadEnv() {
		try {
			var envPath = Paths.get(".env");
			if (Files.exists(envPath)) {
				List<String> lines = Files.readAllLines(envPath);
				for (String line : lines) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) {
						continue;
					}
					int eqIdx = line.indexOf('=');
					if (eqIdx > 0) {
						String key = line.substring(0, eqIdx).trim();
						String value = line.substring(eqIdx + 1).trim();
						
						// Remove potential surrounding quotes
						if (value.startsWith("\"") && value.endsWith("\"")) {
							value = value.substring(1, value.length() - 1);
						} else if (value.startsWith("'") && value.endsWith("'")) {
							value = value.substring(1, value.length() - 1);
						}
						
						// Also support PowerShell prefix if any remains
						if (key.startsWith("$env:")) {
							key = key.substring(5).trim();
						}
						
						// Only set if not already defined in environment or system properties
						if (System.getProperty(key) == null && System.getenv(key) == null) {
							System.setProperty(key, value);
						}
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Failed to load .env file: " + e.getMessage());
		}
	}

}

