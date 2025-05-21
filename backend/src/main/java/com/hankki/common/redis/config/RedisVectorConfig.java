package com.hankki.common.redis.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.StringCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RedisVectorConfig {

    @Bean
    public RedisClient redisClient() {
        try {
            return RedisClient.create("redis://redis:6379");
        } catch (Exception e) {
            log.error("RedisClient 초기화 실패", e);
            throw new IllegalStateException("Redis 서버 연결 실패", e);
        }
    }

    @Bean
    public RedisCommands<String, String> redisCommands(RedisClient redisClient) {
        StatefulRedisConnection<String, String> connection = redisClient.connect(StringCodec.UTF8);
        return connection.sync();
    }
}
