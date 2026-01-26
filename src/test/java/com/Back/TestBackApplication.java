package com.Back;

import org.springframework.boot.SpringApplication;

public class TestBackApplication {

	public static void main(String[] args) {
		SpringApplication.from(BackApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
