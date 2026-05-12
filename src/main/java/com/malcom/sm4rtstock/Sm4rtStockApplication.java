package com.malcom.sm4rtstock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Sm4rtStockApplication {

	public static void main(String[] args) {
		SpringApplication.run(Sm4rtStockApplication.class, args);
	}

}
