package com.hankki.domain.vector.util;

import com.hankki.common.exception.ExceptionStatus;
import com.hankki.common.exception.HankkiWikiException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class VectorAggregator {

    /**
     * 주어진 float 벡터 리스트를 평균 벡터로 그룹화
     */
    /**
     * 주어진 double 벡터 리스트를 평균 벡터로 그룹화
     */
    public static double[] average(List<double[]> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            log.error("[VectorAggregator] 벡터 리스트가 비어있습니다.");
            throw new HankkiWikiException(ExceptionStatus.EMPTY_DIET_REQUEST);
        }

        int dim = vectors.get(0).length;

        // ✅ 벡터 차원 일치 확인
        for (int i = 0; i < vectors.size(); i++) {
            double[] vec = vectors.get(i);
            if (vec.length != dim) {
                log.error("[VectorAggregator] 벡터 차원 불일치: 예상={}, 현재={}, 인덱스={}", dim, vec.length, i);
                throw new HankkiWikiException(ExceptionStatus.INVALID_VECTOR_DIMENSION);
            }
        }

        double[] avg = new double[dim];
        for (double[] vec : vectors) {
            for (int i = 0; i < dim; i++) {
                avg[i] += vec[i];
            }
        }

        for (int i = 0; i < dim; i++) {
            avg[i] /= vectors.size();
        }

        return avg;
    }
}
