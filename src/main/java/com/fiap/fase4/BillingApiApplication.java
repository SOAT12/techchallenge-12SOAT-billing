package com.fiap.fase4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.fiap.fase4.infrastructure.db.repository")
public class BillingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillingApiApplication.class, args);
	}

}
