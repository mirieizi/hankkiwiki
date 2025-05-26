package com.hankki.domain.vector.redis.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.codec.RedisCodec;
import lombok.extern.slf4j.Slf4j;
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

    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient() {
        RedisURI redisUri = RedisURI.Builder
                .redis(redisHost, redisPort)
                .withTimeout(java.time.Duration.ofSeconds(10))
                .build();

        return RedisClient.create(redisUri);
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<byte[], byte[]> redisBinaryConnection(RedisClient redisClient) {
        return redisClient.connect(ByteArrayCodec.INSTANCE);
    }

    @Bean
    public RedisCommands<byte[], byte[]> redisBinaryCommands(
            StatefulRedisConnection<byte[], byte[]> connection
    ) {
        return connection.sync();
    }
}
