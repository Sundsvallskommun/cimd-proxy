package se.sundsvall.cimdproxy;

import org.springframework.boot.SpringApplication;
import se.sundsvall.dept44.ServiceApplication;

@ServiceApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
