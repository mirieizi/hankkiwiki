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

    // float 배열을 Redis 저장용 byte 배열로 변환 (float32 → 4 bytes × N)
    public static byte[] floatArrayToBytes(float[] vector) {
        ByteBuffer buffer = ByteBuffer.allocate(vector.length * 4);
        for (float value : vector) {
            buffer.putFloat(value);
        }
        return buffer.array();
    }

    // Redis에서 가져온 byte[]를 다시 float[]로 변환
    public static float[] bytesToFloatArray(byte[] bytes) {
        int length = bytes.length / 4;
        float[] result = new float[length];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        for (int i = 0; i < length; i++) {
            result[i] = buffer.getFloat();
        }
        return result;
    }
}
