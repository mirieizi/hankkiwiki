package com.hankki;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hankki.domain.user.mapper")
public class HankkiwikiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HankkiwikiApplication.class, args);
	}

}
