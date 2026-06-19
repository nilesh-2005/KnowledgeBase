package com.nilesh.knowledgebase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableJpaAuditing
public class KnowledgebaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(KnowledgebaseApplication.class, args);
	}

}
