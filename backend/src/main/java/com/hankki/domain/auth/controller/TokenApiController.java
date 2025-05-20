package com.hankki.domain.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hankki.domain.auth.dto.request.CreateAccessTokenRequest;
import com.hankki.domain.auth.dto.response.CreateAccessTokenResponse;
import com.hankki.domain.auth.service.TokenService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TokenApiController {
	private final TokenService tokenService;
	
	@PostMapping("/token")
	public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken
	(@RequestBody CreateAccessTokenRequest request){
		String newAccessToken = tokenService.createNewAccessToken(request.getRefreshToken());
		return ResponseEntity.status(HttpStatus.CREATED).body(new CreateAccessTokenResponse(newAccessToken));
	}
}
