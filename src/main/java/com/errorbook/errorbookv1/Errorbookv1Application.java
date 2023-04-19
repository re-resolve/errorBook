package com.errorbook.errorbookv1;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.errorbook.errorbookv1.mapper")
@SpringBootApplication
public class Errorbookv1Application {

	public static void main(String[] args) {
		SpringApplication.run(Errorbookv1Application.class, args);
	}

}
