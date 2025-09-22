package com.fitspine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FitspineApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitspineApplication.class, args);
	}

}
