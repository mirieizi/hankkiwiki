package com.hankki.common.redis.vector;

import com.hankki.domain.user.constant.Gender;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Redis 벡터 검색을 위해 필요한 인덱스를 생성하는 초기화 클래스입니다.
 *
 * - Redis Stack의 RediSearch 기능을 사용하기 위해, 성별(gender)별로 벡터 인덱스를 생성합니다.
 * - 인덱스는 HNSW 알고리즘 기반이며, 벡터 필드(@vector)에 대해 유사도 검색이 가능하도록 설정됩니다.
 * - 애플리케이션 실행 시 자동으로 실행되며, 이미 인덱스가 존재하는 경우에는 생성을 생략합니다.
 *
 * 예:
 *  - 인덱스 이름: idx_male, idx_female
 *  - 키 프리픽스: food_male:, food_female:
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisVectorIndexInitializer {

    private final RedisConnectionFactory redisConnectionFactory;

    @PostConstruct
    public void createVectorIndexes() {

    }

    private void createIndexIfNotExists(Gender gender) {
        String indexName = "idx_" + gender.key();
        String prefix = "food_" + gender.key() + ":";

        try {
            redisConnectionFactory.getConnection().execute("FT.CREATE",
                    indexName.getBytes(), "ON".getBytes(), "HASH".getBytes(),
                    "PREFIX".getBytes(), "1".getBytes(), prefix.getBytes(),
                    "SCHEMA".getBytes(), "vector".getBytes(), "VECTOR".getBytes(),
                    "HNSW".getBytes(), "6".getBytes(),
                    "TYPE".getBytes(), "FLOAT32".getBytes(),
                    "DIM".getBytes(), "9".getBytes(),
                    "DISTANCE_METRIC".getBytes(), "COSINE".getBytes()
            );

            log.info("[RedisVectorIndexInitializer] 인덱스 생성 성공: {} - {}",
                    gender.name().toLowerCase(), indexName);

        } catch (Exception e) {
            if (e.getMessage().contains("Index already exists")) {
                log.info("[RedisVectorIndexInitializer] 인덱스 이미 존재함: {} - {}",
                        gender.name().toLowerCase(), indexName);
            } else {
                log.error("[RedisVectorIndexInitializer] 인덱스 생성 실패: {} - {}",
                        gender.name().toLowerCase(), e.getMessage(), e);
            }
        }
    }
}
