package com.example.notificationconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.notificationconfig")
public class NotificationConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationConfigApplication.class, args);
	}

}
