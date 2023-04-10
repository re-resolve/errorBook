package com.example.errorBook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class ErrorBookApplication {

	public static void main(String[] args) {
		SpringApplication.run(ErrorBookApplication.class, args);
	}

}
