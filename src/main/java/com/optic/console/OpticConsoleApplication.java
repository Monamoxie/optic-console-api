package com.optic.console;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class OpticConsoleApplication {

	public static void main(String[] args) {
		// Set default timezone to UTC for consistent timestamp handling
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		SpringApplication.run(OpticConsoleApplication.class, args);
	}

}
