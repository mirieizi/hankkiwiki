package com.hankki.common.redis.vector;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import com.hankki.common.redis.util.RedisVectorUtil;
import com.hankki.common.vector.VectorAggregator;
import com.hankki.domain.user.constant.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisVectorSearcher {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 주어진 음식 ID 리스트를 기반으로 벡터를 조회하고 평균 벡터 생성
     */
    public float[] computeAverageVector(List<Long> foodIds, Gender gender) {
        List<float[]> vectors = foodIds.stream()
                .map(id -> {
                    String key = String.format("food_%s:%d", gender.key() , id);
                    Object raw = redisTemplate.opsForHash().get(key, "vector");
                    if (raw == null) {
                        throw new HankkiWikiException(ExceptionStatus.NOT_FOUND_VECTOR);
                    }
                    return RedisVectorUtil.bytesToFloatArray((byte[]) raw);
                })
                .collect(Collectors.toList());

        return VectorAggregator.average(vectors);
    }
}
