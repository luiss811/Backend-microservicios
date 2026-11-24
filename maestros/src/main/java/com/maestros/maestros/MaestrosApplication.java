package com.maestros.maestros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MaestrosApplication {

	public static void main(String[] args) {
		SpringApplication.run(MaestrosApplication.class, args);
	}

}
