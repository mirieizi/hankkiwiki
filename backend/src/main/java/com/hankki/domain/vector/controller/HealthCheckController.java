package com.hankki.domain.vector.controller;

import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.output.ArrayOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.ProtocolKeyword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Slf4j
public class HealthCheckController {

    private final RedisCommands<byte[], byte[]> redisCommands;
    private final StringRedisTemplate stringRedisTemplate; // 추가

    private static final ProtocolKeyword FT_LIST = new ProtocolKeyword() {
        private final byte[] raw = "FT._LIST".getBytes(StandardCharsets.UTF_8);
        @Override public byte[] getBytes() { return raw; }
        @Override public String name() { return "FT._LIST"; }
    };

    @GetMapping("/redis-stack")
    public Map<String, Object> checkRedisStack() {
        Map<String, Object> result = new HashMap<>();

        try {
            // StringRedisTemplate으로 간단한 ping 체크
            String ping = stringRedisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            result.put("redis_ping", ping != null ? ping : "PONG");

            // Redis Stack 모듈 확인
            Object ftList = redisCommands.dispatch(
                    FT_LIST,
                    new ArrayOutput<>(ByteArrayCodec.INSTANCE),
                    new CommandArgs<>(ByteArrayCodec.INSTANCE)
            );

            result.put("redis_stack_available", true);
            result.put("ft_list_result", ftList);
            result.put("status", "healthy");

        } catch (Exception e) {
            result.put("redis_stack_available", false);
            result.put("error", e.getMessage());
            result.put("status", "unhealthy");
            log.error("[HealthCheckController] Redis Stack 체크 실패", e);
        }

        return result;
    }

    @GetMapping("/simple")
    public Map<String, Object> simpleHealthCheck() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 가장 간단한 방법
            stringRedisTemplate.opsForValue().set("health:check", "ok");
            String value = stringRedisTemplate.opsForValue().get("health:check");

            result.put("redis_connection", "ok".equals(value) ? "healthy" : "unhealthy");
            result.put("status", "healthy");

        } catch (Exception e) {
            result.put("redis_connection", "unhealthy");
            result.put("error", e.getMessage());
            result.put("status", "unhealthy");
        }

        return result;
    }
}
