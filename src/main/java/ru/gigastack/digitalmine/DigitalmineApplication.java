package ru.gigastack.digitalmine;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.gigastack.digitalmine.service.UserService;

@SpringBootApplication
public class DigitalmineApplication {
	@Bean
	public CommandLineRunner dataInitializer(UserService userService) {
		return args -> {
			if (userService.findByUsername("admin").isEmpty()) {
				userService.registerUser("admin", "admin", "ADMIN");
			}
		};
	}
	public static void main(String[] args) {

		SpringApplication.run(DigitalmineApplication.class, args);

	}

}
