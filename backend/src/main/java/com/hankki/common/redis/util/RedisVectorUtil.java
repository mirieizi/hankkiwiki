package com.hankki.common.redis.util;

import java.nio.ByteBuffer;

/**
 * Redis 벡터 검색 기능은 float32 형식의 raw binary 데이터를 필요로 합니다.
 * 따라서 Java의 float[] 배열을 Redis에 저장하려면 byte[]로 변환해야 합니다.
 *
 * 반대로, Redis에서 벡터 데이터를 읽어올 경우에는 byte[]를 다시 float[]로 복원해야 합니다.
 *
 * 이 클래스는 float 배열을 byte 배열로 변환하거나, 그 반대 방향으로 변환하는 유틸리티 기능을 제공합니다.
 */
public class RedisVectorUtil {

    // double 배열을 Redis 저장용 byte 배열로 변환 (float32 → 4 bytes × N)
    public static byte[] doubleArrayToBytes(double[] vector) {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES * vector.length);
        for (double v : vector) {
            buffer.putDouble(v);
        }
        return buffer.array();
    }

    // Redis에서 가져온 byte[]를 다시 double[]로 변환
    public static double[] bytesToDoubleArray(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int len = bytes.length / Double.BYTES;
        double[] result = new double[len];
        for (int i = 0; i < len; i++) {
            result[i] = buffer.getDouble();
        }
        return result;
    }
}
