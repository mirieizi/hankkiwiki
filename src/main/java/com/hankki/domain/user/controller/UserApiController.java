package com.hankki.domain.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hankki.domain.user.dto.SignUpRequest;
import com.hankki.domain.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserApiController {
	private final UserService userService;
	
	
	@PostMapping("/sign")
	public String signup(SignUpRequest request) {
		userService.signUp(request);
		return "redirect:/login";
	}
	
	@GetMapping("/login")
	public String login() {
		return "login";
	}
	@GetMapping("/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
		return "redirect:/login";
	}
	
}
