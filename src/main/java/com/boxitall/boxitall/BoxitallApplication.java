package com.boxitall.boxitall;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BoxitallApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMalformed()
				.ignoreIfMissing()
				.load();

		// Load env vars into JVM
		dotenv.entries().forEach((entry) -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});

		SpringApplication.run(BoxitallApplication.class, args);
		System.out.println("BoxitAll corriendo");
	}

}
