package org.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);

		System.out.println("User Service is running...");
		System.out.println("Access the User Service at: http://localhost:8090/");

	}

}
