package com.todoapp.todorails;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.security.Security;

@SpringBootApplication
@EnableScheduling
public class TodoRailsApplication {

	public static void main(String[] args) {

		if(Security.getProvider("BC") == null){
			Security.addProvider(new BouncyCastleProvider());
			System.out.println("Bouncy security provider registered successfully.");
		}

		SpringApplication.run(TodoRailsApplication.class, args);
	}

}
