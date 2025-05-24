package com.hankki.domain.vector.redis.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
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
    public RedisCommands<byte[], byte[]> redisBinaryCommands(RedisClient redisClient) {
        try {
            StatefulRedisConnection<byte[], byte[]> connection =
                    redisClient.connect(ByteArrayCodec.INSTANCE); // ✅ 반드시 여기!
            return connection.sync();
        } catch (Exception e) {
            log.error("RedisVectorCommand 초기화 실패", e);
            throw new IllegalStateException("Redis Binary 연결 실패", e);
        }
    }

}
