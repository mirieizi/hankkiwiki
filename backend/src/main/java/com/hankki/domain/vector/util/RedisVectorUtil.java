package com.hankki.domain.vector.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Slf4j
public class RedisVectorUtil {

    /**
     * double 배열을 float32 바이트 배열로 변환
     * Redis HNSW 인덱스는 정확히 4 * dimension 바이트를 요구함
     */
    public static byte[] doubleToFloatBytes(double[] vector) {
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("벡터가 null이거나 비어있습니다.");
        }

        ByteBuffer buffer = ByteBuffer.allocate(vector.length * 4)
                .order(ByteOrder.LITTLE_ENDIAN);

        for (double value : vector) {
            // NaN이나 무한대 값 검증
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                log.warn("[RedisVectorUtil] 잘못된 벡터 값 감지: {}", value);
                buffer.putFloat(0.0f); // 기본값으로 대체
            } else {
                buffer.putFloat((float) value);
            }
        }

        byte[] result = buffer.array();
        log.debug("[RedisVectorUtil] 벡터 변환 완료: 입력 차원={}, 출력 바이트={}",
                vector.length, result.length);
        return result;
    }

    /**
     * 바이트 배열을 double 배열로 역변환 (검증용)
     */
    public static double[] bytesToDoubleArray(byte[] bytes) {
        if (bytes == null || bytes.length % 4 != 0) {
            throw new IllegalArgumentException("잘못된 바이트 배열입니다. 길이: " +
                    (bytes != null ? bytes.length : "null"));
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        double[] result = new double[bytes.length / 4];

        for (int i = 0; i < result.length; i++) {
            result[i] = buffer.getFloat();
        }

        log.debug("[RedisVectorUtil] 바이트 배열 역변환 완료: 바이트 크기={}, 벡터 차원={}",
                bytes.length, result.length);
        return result;
    }

    /**
     * 벡터 유효성 검증
     */
    public static boolean isValidVector(double[] vector, int expectedDimension) {
        if (vector == null || vector.length != expectedDimension) {
            return false;
        }

        for (double value : vector) {
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 벡터 정규화 (선택사항)
     */
    public static double[] normalizeVector(double[] vector) {
        if (vector == null || vector.length == 0) {
            return vector;
        }

        double norm = 0.0;
        for (double value : vector) {
            norm += value * value;
        }

        norm = Math.sqrt(norm);
        if (norm == 0.0) {
            log.warn("[RedisVectorUtil] 영벡터는 정규화할 수 없습니다");
            return vector;
        }

        double[] normalized = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = vector[i] / norm;
        }

        return normalized;
    }
}
