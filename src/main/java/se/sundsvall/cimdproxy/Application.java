package se.sundsvall.cimdproxy;

import static org.springframework.boot.SpringApplication.run;

import se.sundsvall.dept44.ServiceApplication;

@ServiceApplication
public class Application {
	public static void main(String... args) {
		run(Application.class, args);
	}
}
