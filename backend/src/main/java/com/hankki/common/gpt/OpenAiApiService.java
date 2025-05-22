package com.hankki.common.gpt;

import java.util.List;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.hankki.common.config.GptConfig;

import lombok.RequiredArgsConstructor;

/**
 * 프롬프트, 모델명, api 키를 받아서 실제 openai http 요청
 */

@Service
@RequiredArgsConstructor
public class OpenAiApiService {

	private final GptConfig gptConfig;
	private final RestTemplate restTemplate = new RestTemplate();
	
	public String requestChatCompletion(String prompt) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(gptConfig.getSecretKey());
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		// 메시지 설정
		JSONObject body = new JSONObject();
		body.put("model", gptConfig.getModel());
		// system: ai의 기본 성격, 역할, 스타일, 기준 등을 정하는 프롬프트
		// user : 실제 사용자의 요청 및 질문
		body.put("messages", List.of(
				java.util.Map.of("role", "system", "content", "너는 건강 식단 추천 전문가야"),
				java.util.Map.of("role", "user", "content", prompt)));
		HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
		ResponseEntity<String> response = restTemplate.postForEntity(
				"https://api.openai.com/v1/chat/completions", entity, String.class);
		// 응답 받기
		return extraAnswerFromOpenAiResponse(response.getBody());
		
	}

	private String extraAnswerFromOpenAiResponse(String responseBody) {
		JSONObject json = new JSONObject(responseBody);
		return json.getJSONArray("choices")
				.getJSONObject(0)
				.getJSONObject("message")
				.getString("content");
	}
	
	
}
