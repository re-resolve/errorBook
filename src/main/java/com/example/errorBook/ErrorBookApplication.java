package com.example.errorBook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@MapperScan("com.example.errorBook.mapper")

@SpringBootApplication
//@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class ErrorBookApplication {

	public static void main(String[] args) {
		SpringApplication.run(ErrorBookApplication.class, args);
	}

}
