package com.fiap.fase4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.fiap.fase4.infrastructure.db.repository")
@EnableAsync
public class BillingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillingApiApplication.class, args);
	}

}
