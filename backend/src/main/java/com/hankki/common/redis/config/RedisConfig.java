package com.hankki.common.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Spring Boot 애플리케이션에서 Redis와의 연결을 설정하는 구성 클래스입니다.
 *
 * - Redis 서버와의 연결을 위한 `LettuceConnectionFactory`를 정의합니다.
 *   기본 설정은 localhost:6379이며, application.yml에서 오버라이드할 수 있습니다.
 *
 * - Redis와 데이터를 주고받기 위한 `RedisTemplate<String, Object>` Bean을 제공합니다.
 *   이 템플릿은 해시(Hash) 구조에서 vector 데이터를 저장하거나 조회할 때 사용됩니다.
 *   벡터는 byte[] 형식으로 저장되기 때문에 ValueSerializer, HashValueSerializer는 byte-array 기반으로 설정되어 있습니다.
 *
 * - 문자열 전용의 간단한 Redis 연산을 위한 `StringRedisTemplate`도 함께 등록됩니다.
 *
 * 이 구성은 Redis Stack 및 벡터 검색(벡터 저장 및 인덱싱)을 사용하는 환경에 적합하며,
 * Redis에 벡터 데이터를 효율적으로 저장하고 관리할 수 있도록 지원합니다.
 */
@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redistConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379));
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // Key, Value 직렬화
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializer.byteArray()); // 벡터 저장
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setHashValueSerializer(RedisSerializer.byteArray());

        return redisTemplate;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

}
