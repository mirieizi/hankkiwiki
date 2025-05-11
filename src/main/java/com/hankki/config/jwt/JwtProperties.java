// src/main/java/com/hankki/config/jwt/JwtProperties.java
package com.hankki.config.jwt;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
@ConfigurationProperties("jwt")
public class JwtProperties {
    private String issuer;
    private String secretKey;

    /** AccessToken 만료기간 (spring binding으로 Duration 지원) */
    private Duration accessTokenValidity;   

    /** RefreshToken 만료기간 */
    private Duration refreshTokenValidity; 
}
