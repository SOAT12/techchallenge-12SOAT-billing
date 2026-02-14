package com.fiap.fase4.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "com.fiap.fase4")
@EnableMongoRepositories(basePackages = "com.fiap.fase4.repository")
public class BillingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillingApiApplication.class, args);
	}

}
