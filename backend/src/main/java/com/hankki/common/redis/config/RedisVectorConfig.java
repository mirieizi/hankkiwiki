package com.hankki.common.redis.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.StringCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisVectorConfig {

    @Bean
    public RedisClient redisClient() {
        return RedisClient.create("redis://localhost:6379"); // or your Redis URI
    }

    @Bean
    public RedisCommands<String, String> redisCommands(RedisClient redisClient) {
        StatefulRedisConnection<String, String> connection = redisClient.connect(StringCodec.UTF8);
        return connection.sync();
    }
}
