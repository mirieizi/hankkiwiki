package com.hankki.domain.user.service;

import java.util.List;

import com.hankki.domain.user.entity.User;

public interface UserService {
	public User signup (User user);
	public User login(User user);
	public User getById(Long id);
}
