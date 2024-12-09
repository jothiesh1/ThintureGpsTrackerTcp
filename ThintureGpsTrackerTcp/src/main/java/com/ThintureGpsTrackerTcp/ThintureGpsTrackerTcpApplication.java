package com.ThintureGpsTrackerTcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
//@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})

 @SpringBootApplication
public class ThintureGpsTrackerTcpApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThintureGpsTrackerTcpApplication.class, args);
	}

}
