package com.practice.nexora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NexoraStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(NexoraStoreApplication.class, args);
	}

}
