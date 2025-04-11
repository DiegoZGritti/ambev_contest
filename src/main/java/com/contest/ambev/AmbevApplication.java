package com.contest.ambev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.contest.ambev.adapters.output.persistence"})
@EnableRetry
@EnableScheduling
public class AmbevApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmbevApplication.class, args);
	}

}

