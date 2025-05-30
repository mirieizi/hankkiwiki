package com.hankki.domain.vector.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Slf4j
public class RedisVectorUtil {

    /**
     * double 배열을 float32 바이트 배열로 변환
     * Redis HNSW/FLAT 인덱스는 정확히 4 * dimension 바이트를 요구함
     */
    public static byte[] doubleToFloatBytes(double[] vector) {
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("벡터가 null이거나 비어있습니다.");
        }
        // 인덱스 기반 for문이 약간 더 빠름
        ByteBuffer buffer = ByteBuffer.allocate(vector.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < vector.length; i++) {
            double value = vector[i];
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                // log.warn은 남겨두되, 대량 호출시엔 주석처리 가능
                log.warn("[RedisVectorUtil] 잘못된 벡터 값 감지: {}", value);
                buffer.putFloat(0.0f);
            } else {
                buffer.putFloat((float) value);
            }
        }
        // 변환 성공 로그는 대량 호출시엔 비활성화
        // log.debug("[RedisVectorUtil] 벡터 변환 완료: 입력 차원={}, 출력 바이트={}", vector.length, buffer.array().length);
        return buffer.array();
    }

    /**
     * 바이트 배열을 double 배열로 역변환 (검증용)
     */
    public static double[] bytesToDoubleArray(byte[] bytes) {
        if (bytes == null || bytes.length % 4 != 0) {
            throw new IllegalArgumentException("잘못된 바이트 배열입니다. 길이: " +
                    (bytes != null ? bytes.length : "null"));
        }
        int dim = bytes.length / 4;
        double[] result = new double[dim];
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < dim; i++) {
            result[i] = buffer.getFloat();
        }
        // log.debug("[RedisVectorUtil] 바이트 배열 역변환 완료: 바이트 크기={}, 벡터 차원={}", bytes.length, dim);
        return result;
    }

    /**
     * 벡터 유효성 검증
     */
    public static boolean isValidVector(double[] vector, int expectedDimension) {
        if (vector == null || vector.length != expectedDimension) return false;
        for (int i = 0; i < vector.length; i++) {
            double value = vector[i];
            if (Double.isNaN(value) || Double.isInfinite(value)) return false;
        }
        return true;
    }

    /**
     * 벡터 정규화 (선택사항)
     */
    public static double[] normalizeVector(double[] vector) {
        if (vector == null || vector.length == 0) return vector;
        double norm = 0.0;
        for (int i = 0; i < vector.length; i++) {
            norm += vector[i] * vector[i];
        }
        if (norm == 0.0) {
            log.warn("[RedisVectorUtil] 영벡터는 정규화할 수 없습니다");
            return vector;
        }
        norm = Math.sqrt(norm);
        double[] normalized = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = vector[i] / norm;
        }
        return normalized;
    }
}
