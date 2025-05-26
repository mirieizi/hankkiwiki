package com.hankki.domain.vector.redis.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RedisVectorConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient() {
        String uri = String.format("redis://%s:%d", redisHost, redisPort);
        return RedisClient.create(uri);
    }

    @Bean
    public StatefulRedisConnection<byte[], byte[]> redisBinaryConnection(RedisClient redisClient) {
        try {
            return redisClient.connect(ByteArrayCodec.INSTANCE);
        } catch (Exception e) {
            log.error("RedisConnection 초기화 실패", e);
            throw new IllegalStateException("Redis Binary 연결 실패", e);
        }
    }

    @Bean
    public RedisCommands<byte[], byte[]> redisBinaryCommands(
            StatefulRedisConnection<byte[], byte[]> connection
    ) {
        return connection.sync(); // sync 커맨드 객체 반환
    }
}
