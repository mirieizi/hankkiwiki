package com.hankki.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;

@Getter
@Configuration
public class GptConfig {
	@Value("${spring.ai.openai.api-key}")
	private String secretKey;

	@Value("${spring.ai.openai.model}")
	private String model;

	
	/**
	 * 외부 REST API와 통신할 때 spring에서 공식적으로 제공하는 HTTP 요청용 도구
	 * @return
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
