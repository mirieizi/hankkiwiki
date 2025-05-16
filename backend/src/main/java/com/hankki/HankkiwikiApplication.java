package com.hankki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = {"com.hankki"})
public class HankkiwikiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HankkiwikiApplication.class, args);
	}

}
