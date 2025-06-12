package com.hankki.domain.vector.redis.config;

import com.hankki.domain.user.constant.Gender;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.output.ArrayOutput;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.ProtocolKeyword;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisVectorIndexInitializer {

    private final RedisClient redisClient;

    // ProtocolKeyword 정의
    private static final ProtocolKeyword FT_CREATE = new ProtocolKeyword() {
        private final byte[] raw = "FT.CREATE".getBytes(StandardCharsets.UTF_8);
        @Override public byte[] getBytes() { return raw; }
        @Override public String name() { return "FT.CREATE"; }
    };

    private static final ProtocolKeyword FT_INFO = new ProtocolKeyword() {
        private final byte[] raw = "FT.INFO".getBytes(StandardCharsets.UTF_8);
        @Override public byte[] getBytes() { return raw; }
        @Override public String name() { return "FT.INFO"; }
    };

    private static final ProtocolKeyword FT_LIST = new ProtocolKeyword() {
        private final byte[] raw = "FT._LIST".getBytes(StandardCharsets.UTF_8);
        @Override public byte[] getBytes() { return raw; }
        @Override public String name() { return "FT._LIST"; }
    };

    @PostConstruct
    public void createVectorIndexes() {
        RedisCommands<byte[], byte[]> redis = redisClient.connect(ByteArrayCodec.INSTANCE).sync();

        // Redis Stack 모듈 지원 여부 확인
        if (!checkRedisStackSupport(redis)) {
            log.error("[RedisVectorIndexInitializer] Redis Stack 모듈이 지원되지 않습니다. redis-stack-server를 사용하세요.");
            return;
        }

        String indexName = "idx_food_vector";
        List<String> keyPrefixes = new ArrayList<>();
        for (Gender gender : Gender.values()) {
            keyPrefixes.add("food_" + gender.key() + ":");
        }

        // 인덱스가 이미 존재하면 생성하지 않음
        if (indexExists(redis, indexName)) {
            log.info("[RedisVectorIndexInitializer] 인덱스 이미 존재: {}", indexName);
            return;
        }

        // 인덱스 생성
        createVectorIndex(redis, indexName, keyPrefixes);

        // 인덱스 상태 확인
        checkIndexInfoSafely(redis, indexName);
    }

    private boolean checkRedisStackSupport(RedisCommands<byte[], byte[]> redis) {
        try {
            redis.dispatch(
                    FT_LIST,
                    new ArrayOutput<>(ByteArrayCodec.INSTANCE),
                    new CommandArgs<>(ByteArrayCodec.INSTANCE)
            );
            log.info("[RedisVectorIndexInitializer] Redis Stack 모듈 지원 확인됨");
            return true;
        } catch (Exception e) {
            log.error("[RedisVectorIndexInitializer] Redis Stack 모듈 미지원: {}", e.getMessage());
            return false;
        }
    }

    private boolean indexExists(RedisCommands<byte[], byte[]> redis, String indexName) {
        try {
            redis.dispatch(
                    FT_INFO,
                    new ArrayOutput<>(ByteArrayCodec.INSTANCE),
                    new CommandArgs<>(ByteArrayCodec.INSTANCE)
                            .add(indexName.getBytes(StandardCharsets.UTF_8))
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void createVectorIndex(RedisCommands<byte[], byte[]> redis, String indexName, List<String> keyPrefixes) {
        try {
            CommandArgs<byte[], byte[]> args = new CommandArgs<>(ByteArrayCodec.INSTANCE)
                    .add(indexName.getBytes(StandardCharsets.UTF_8))
                    .add("ON".getBytes(StandardCharsets.UTF_8))
                    .add("HASH".getBytes(StandardCharsets.UTF_8))
                    .add("PREFIX".getBytes(StandardCharsets.UTF_8))
                    .add(keyPrefixes.size());
            for (String prefix : keyPrefixes) {
                args.add(prefix.getBytes(StandardCharsets.UTF_8));
            }
            args.add("SCHEMA".getBytes(StandardCharsets.UTF_8))
                    .add("vector".getBytes(StandardCharsets.UTF_8))
                    .add("VECTOR".getBytes(StandardCharsets.UTF_8))
                    .add("FLAT".getBytes(StandardCharsets.UTF_8)) // FLAT 인덱스 사용
                    .add(6)
                    .add("TYPE".getBytes(StandardCharsets.UTF_8))
                    .add("FLOAT32".getBytes(StandardCharsets.UTF_8))
                    .add("DIM".getBytes(StandardCharsets.UTF_8))
                    .add(9)
                    .add("DISTANCE_METRIC".getBytes(StandardCharsets.UTF_8))
                    .add("COSINE".getBytes(StandardCharsets.UTF_8));

            redis.dispatch(
                    FT_CREATE,
                    new StatusOutput<>(ByteArrayCodec.INSTANCE),
                    args
            );
            log.info("[RedisVectorIndexInitializer] 벡터 인덱스 생성 완료: {}", indexName);
        } catch (Exception e) {
            log.error("[RedisVectorIndexInitializer] 인덱스 생성 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void checkIndexInfoSafely(RedisCommands<byte[], byte[]> redis, String indexName) {
        try {
            Thread.sleep(100);

            Object info = redis.dispatch(
                    FT_INFO,
                    new ArrayOutput<>(ByteArrayCodec.INSTANCE),
                    new CommandArgs<>(ByteArrayCodec.INSTANCE)
                            .add(indexName.getBytes(StandardCharsets.UTF_8))
            );

            if (info instanceof java.util.List) {
                java.util.List<?> infoList = (java.util.List<?>) info;
                log.info("[RedisVectorIndexInitializer] 인덱스 생성 확인: {} (요소 수: {})",
                        indexName, infoList.size());
                logIndexDetails(indexName, infoList);
            } else {
                log.info("[RedisVectorIndexInitializer] 인덱스 생성 확인: {}", indexName);
            }

        } catch (Exception e) {
            log.warn("[RedisVectorIndexInitializer] 인덱스 정보 조회 실패 (하지만 생성은 성공했을 수 있음): index={}, error={}",
                    indexName, e.getMessage());
        }
    }

    private void logIndexDetails(String indexName, java.util.List<?> infoList) {
        try {
            for (int i = 0; i < infoList.size() - 1; i++) {
                if (infoList.get(i) instanceof byte[]) {
                    String key = new String((byte[]) infoList.get(i), StandardCharsets.UTF_8);
                    Object value = infoList.get(i + 1);

                    if ("index_name".equals(key) || "num_docs".equals(key) || "num_terms".equals(key)) {
                        log.info("[RedisVectorIndexInitializer] 인덱스 {} 정보 - {}: {}",
                                indexName, key, value);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("[RedisVectorIndexInitializer] 인덱스 세부 정보 파싱 실패: {}", e.getMessage());
        }
    }
}
