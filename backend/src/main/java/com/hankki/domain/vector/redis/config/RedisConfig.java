package com.hankki.domain.vector.redis.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.ssl:false}")
    private boolean redisSsl;

    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient() {
        if (redisHost == null || redisHost.isEmpty()) {
            throw new IllegalArgumentException("Redis host is not set!");
        }
        if (redisPort <= 0) {
            throw new IllegalArgumentException("Redis port is not set or invalid!");
        }

        RedisURI.Builder builder = RedisURI.Builder
                .redis(redisHost, redisPort)
                .withTimeout(java.time.Duration.ofSeconds(10));

        if (redisSsl) {
            builder.withSsl(true);
        }

        RedisURI redisUri = builder.build();

        log.info("[RedisConfig] RedisClient 생성: {}:{}", redisHost, redisPort);
        return RedisClient.create(redisUri);
    }

    /**
     * Lettuce의 싱글 커넥션 빈 (소규모 트래픽에 적합)
     */
    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<byte[], byte[]> redisBinaryConnection(RedisClient redisClient) {
        log.info("[RedisConfig] Redis Binary 커넥션 생성");
        return redisClient.connect(ByteArrayCodec.INSTANCE);
    }

    @Bean
    public RedisCommands<byte[], byte[]> redisBinaryCommands(
            StatefulRedisConnection<byte[], byte[]> connection
    ) {
        return connection.sync();
    }

}
