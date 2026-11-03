package com.practice.nexora;

import org.springframework.boot.SpringApplication;

public class TestNexoraStoreApplication {

	public static void main(String[] args) {
		SpringApplication.from(NexoraStoreApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
