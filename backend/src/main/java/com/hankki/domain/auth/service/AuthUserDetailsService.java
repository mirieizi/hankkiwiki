package com.hankki.domain.auth.service;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.entity.UserHealthInfo;
import com.hankki.domain.user.repository.UserHealthInfoRepository;
import com.hankki.domain.user.repository.UserRepository;
import com.hankki.domain.auth.entity.AuthUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserHealthInfoRepository userHealthInfoRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_USER));

        UserHealthInfo info = userHealthInfoRepository.findByUserId(user.getId())
                .orElseThrow(() -> new HankkiWikiException(ExceptionStatus.NOT_FOUND_USER_HEALTH));

        return new AuthUser(user, info.getGender());
    }
}
