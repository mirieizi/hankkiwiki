package com.hankki.domain.recommend.controller;

import com.hankki.domain.user.constant.Gender;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.output.ArrayOutput;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.ProtocolKeyword;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

// 별도의 디버그 컨트롤러 생성 (권장)
@RestController
@RequestMapping("/admin/debug")
@RequiredArgsConstructor
public class VectorDebugController {

    private final RedisClient redisClient;
    // ProtocolKeyword 정의
    private static final ProtocolKeyword FT_INFO = new ProtocolKeyword() {
        private final byte[] raw = "FT.INFO".getBytes(StandardCharsets.UTF_8);
        @Override public byte[] getBytes() { return raw; }
        @Override public String name() { return "FT.INFO"; }
    };

    private static final ProtocolKeyword FT_SEARCH = new ProtocolKeyword() {
        private final byte[] raw = "FT.SEARCH".getBytes(StandardCharsets.UTF_8);
        @Override public byte[] getBytes() { return raw; }
        @Override public String name() { return "FT.SEARCH"; }
    };

    private static final ProtocolKeyword FT_DROPINDEX = new ProtocolKeyword() {
        private final byte[] raw = "FT.DROPINDEX".getBytes(StandardCharsets.UTF_8);
        @Override public byte[] getBytes() { return raw; }
        @Override public String name() { return "FT.DROPINDEX"; }
    };

    @GetMapping("/redis-search-test")
    public ResponseEntity<Map<String, Object>> testRedisSearch() {
        Map<String, Object> result = new HashMap<>();

        try {
            RedisCommands<byte[], byte[]> redis = redisClient.connect(ByteArrayCodec.INSTANCE).sync();

            // 1. 전체 벡터 키 확인
            List<byte[]> allVectorKeys = redis.keys("food_*".getBytes(StandardCharsets.UTF_8));
            result.put("totalVectorKeys", allVectorKeys.size());

            // 2. 성별별 벡터 키 확인
            long maleKeys = allVectorKeys.stream()
                    .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                    .filter(key -> key.startsWith("food_male:"))
                    .count();

            long femaleKeys = allVectorKeys.stream()
                    .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                    .filter(key -> key.startsWith("food_female:"))
                    .count();

            result.put("maleVectorKeys", maleKeys);
            result.put("femaleVectorKeys", femaleKeys);

            // 3. 샘플 키들 조회
            List<String> sampleKeys = allVectorKeys.stream()
                    .limit(10)
                    .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                    .collect(Collectors.toList());
            result.put("sampleKeys", sampleKeys);

            // 4. 통합 인덱스 존재 확인
            result.put("unifiedIndex", checkIndexExists(redis, "idx_food_vector"));

            // 5. 개별 인덱스 존재 확인
            result.put("maleIndex", checkIndexExists(redis, "idx_male"));
            result.put("femaleIndex", checkIndexExists(redis, "idx_female"));

            // 6. 실제 벡터 데이터 샘플 조회
            if (!sampleKeys.isEmpty()) {
                String sampleKey = sampleKeys.get(0);
                byte[] vectorData = redis.hget(
                        sampleKey.getBytes(StandardCharsets.UTF_8),
                        "vector".getBytes(StandardCharsets.UTF_8)
                );
                result.put("sampleVectorExists", vectorData != null);
                if (vectorData != null) {
                    result.put("sampleVectorSize", vectorData.length);
                }
            }

        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/vector-status")
    public ResponseEntity<Map<String, Object>> getVectorStatus() {
        Map<String, Object> result = new HashMap<>();

        try {
            RedisCommands<byte[], byte[]> redis = redisClient.connect(ByteArrayCodec.INSTANCE).sync();

            // 각 성별별 상세 정보
            for (Gender gender : Gender.values()) {
                Map<String, Object> genderInfo = new HashMap<>();

                String keyPattern = "food_" + gender.key() + ":*";
                List<byte[]> genderKeys = redis.keys(keyPattern.getBytes(StandardCharsets.UTF_8));

                genderInfo.put("keyCount", genderKeys.size());
                genderInfo.put("keyPattern", keyPattern);

                // 첫 5개 키 샘플
                List<String> sampleKeys = genderKeys.stream()
                        .limit(5)
                        .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                        .collect(Collectors.toList());
                genderInfo.put("sampleKeys", sampleKeys);

                result.put(gender.key(), genderInfo);
            }

            // 인덱스 상태 상세 정보
            Map<String, Object> indexInfo = new HashMap<>();
            indexInfo.put("unified", getIndexDetails(redis, "idx_food_vector"));
            indexInfo.put("male", getIndexDetails(redis, "idx_male"));
            indexInfo.put("female", getIndexDetails(redis, "idx_female"));

            result.put("indexes", indexInfo);

        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/test-vector-search")
    public ResponseEntity<Map<String, Object>> testVectorSearch(
            @RequestParam(defaultValue = "MALE") String gender,
            @RequestParam(defaultValue = "5") int limit) {

        Map<String, Object> result = new HashMap<>();

        try {
            RedisCommands<byte[], byte[]> redis = redisClient.connect(ByteArrayCodec.INSTANCE).sync();

            Gender genderEnum = Gender.valueOf(gender.toUpperCase());
            result.put("searchGender", genderEnum);
            result.put("searchLimit", limit);

            // 테스트용 더미 벡터 (9차원)
            float[] testVector = {0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f};
            byte[] vectorBytes = floatArrayToBytes(testVector);

            // 1. 통합 인덱스로 검색 시도
            try {
                List<String> unifiedResults = performVectorSearch(redis, "idx_food_vector",
                        vectorBytes, limit, genderEnum);
                result.put("unifiedIndexResults", unifiedResults);
            } catch (Exception e) {
                result.put("unifiedIndexError", e.getMessage());
            }

            // 2. 개별 인덱스로 검색 시도
            try {
                String individualIndex = "idx_" + genderEnum.key();
                List<String> individualResults = performVectorSearch(redis, individualIndex,
                        vectorBytes, limit, genderEnum);
                result.put("individualIndexResults", individualResults);
            } catch (Exception e) {
                result.put("individualIndexError", e.getMessage());
            }

        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/clear-vector-data")
    public ResponseEntity<Map<String, Object>> clearVectorData() {
        Map<String, Object> result = new HashMap<>();

        try {
            RedisCommands<byte[], byte[]> redis = redisClient.connect(ByteArrayCodec.INSTANCE).sync();

            // 1. 벡터 키들 삭제
            List<byte[]> vectorKeys = redis.keys("food_*".getBytes(StandardCharsets.UTF_8));
            int vectorKeyCount = vectorKeys.size();

            if (!vectorKeys.isEmpty()) {
                Long deletedKeys = redis.del(vectorKeys.toArray(new byte[0][]));
                result.put("deletedVectorKeys", deletedKeys);
            } else {
                result.put("deletedVectorKeys", 0);
            }

            // 2. 인덱스들 삭제 시도
            List<String> indexesToDrop = Arrays.asList("idx_food_vector", "idx_male", "idx_female");
            Map<String, String> dropResults = new HashMap<>();

            for (String indexName : indexesToDrop) {
                try {
                    redis.dispatch(
                            FT_DROPINDEX,
                            new StatusOutput<>(ByteArrayCodec.INSTANCE),
                            new CommandArgs<>(ByteArrayCodec.INSTANCE)
                                    .add(indexName.getBytes(StandardCharsets.UTF_8))
                    );
                    dropResults.put(indexName, "삭제 성공");
                } catch (Exception e) {
                    dropResults.put(indexName, "삭제 실패: " + e.getMessage());
                }
            }

            result.put("indexDropResults", dropResults);
            result.put("status", "success");
            result.put("message", String.format("벡터 키 %d개 정리 완료", vectorKeyCount));

        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/clear-all-redis")
    public ResponseEntity<String> clearAllRedis() {
        try {
            RedisCommands<byte[], byte[]> redis = redisClient.connect(ByteArrayCodec.INSTANCE).sync();
            redis.flushdb();
            return ResponseEntity.ok("Redis 데이터베이스 전체 삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("삭제 실패: " + e.getMessage());
        }
    }

    // === 헬퍼 메서드들 ===

    private Map<String, Object> checkIndexExists(RedisCommands<byte[], byte[]> redis, String indexName) {
        Map<String, Object> indexStatus = new HashMap<>();
        try {
            Object info = redis.dispatch(
                    FT_INFO,
                    new ArrayOutput<>(ByteArrayCodec.INSTANCE),
                    new CommandArgs<>(ByteArrayCodec.INSTANCE)
                            .add(indexName.getBytes(StandardCharsets.UTF_8))
            );
            indexStatus.put("exists", true);
            indexStatus.put("info", "인덱스 존재");
        } catch (Exception e) {
            indexStatus.put("exists", false);
            indexStatus.put("error", e.getMessage());
        }
        return indexStatus;
    }

    private Map<String, Object> getIndexDetails(RedisCommands<byte[], byte[]> redis, String indexName) {
        Map<String, Object> details = new HashMap<>();
        try {
            Object info = redis.dispatch(
                    FT_INFO,
                    new ArrayOutput<>(ByteArrayCodec.INSTANCE),
                    new CommandArgs<>(ByteArrayCodec.INSTANCE)
                            .add(indexName.getBytes(StandardCharsets.UTF_8))
            );

            details.put("exists", true);
            if (info instanceof List) {
                List<?> infoList = (List<?>) info;
                details.put("infoSize", infoList.size());

                // num_docs 찾기
                for (int i = 0; i < infoList.size() - 1; i++) {
                    if (infoList.get(i) instanceof byte[]) {
                        String key = new String((byte[]) infoList.get(i), StandardCharsets.UTF_8);
                        if ("num_docs".equals(key)) {
                            details.put("numDocs", infoList.get(i + 1));
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            details.put("exists", false);
            details.put("error", e.getMessage());
        }
        return details;
    }

    private List<String> performVectorSearch(RedisCommands<byte[], byte[]> redis, String indexName,
                                             byte[] vectorBytes, int limit, Gender gender) {
        try {
            String query;
            if ("idx_food_vector".equals(indexName)) {
                // 통합 인덱스용 쿼리
                query = String.format("@__key:food_%s\\:* => [KNN %d @vector $BLOB]",
                        gender.key(), limit);
            } else {
                // 개별 인덱스용 쿼리
                query = String.format("*=>[KNN %d @vector $BLOB]", limit);
            }

            Object result = redis.dispatch(
                    FT_SEARCH,
                    new ArrayOutput<>(ByteArrayCodec.INSTANCE),
                    new CommandArgs<>(ByteArrayCodec.INSTANCE)
                            .add(indexName.getBytes(StandardCharsets.UTF_8))
                            .add(query.getBytes(StandardCharsets.UTF_8))
                            .add("PARAMS".getBytes(StandardCharsets.UTF_8)).add(2)
                            .add("BLOB".getBytes(StandardCharsets.UTF_8)).add(vectorBytes)
                            .add("RETURN".getBytes(StandardCharsets.UTF_8)).add(1)
                            .add("__key".getBytes(StandardCharsets.UTF_8))
                            .add("LIMIT".getBytes(StandardCharsets.UTF_8))
                            .add(0).add(limit)
            );

            return parseSearchResult(result);

        } catch (Exception e) {
            throw new RuntimeException("검색 실패: " + e.getMessage(), e);
        }
    }

    private List<String> parseSearchResult(Object result) {
        List<String> keys = new ArrayList<>();
        if (result instanceof List) {
            List<?> resultList = (List<?>) result;
            for (int i = 1; i < resultList.size(); i += 2) { // 첫 번째는 총 개수, 그 다음부터 키-값 쌍
                if (resultList.get(i) instanceof byte[]) {
                    String key = new String((byte[]) resultList.get(i), StandardCharsets.UTF_8);
                    keys.add(key);
                }
            }
        }
        return keys;
    }

    private byte[] floatArrayToBytes(float[] floats) {
        byte[] bytes = new byte[floats.length * 4];
        for (int i = 0; i < floats.length; i++) {
            int intBits = Float.floatToIntBits(floats[i]);
            bytes[i * 4] = (byte) (intBits & 0xFF);
            bytes[i * 4 + 1] = (byte) ((intBits >> 8) & 0xFF);
            bytes[i * 4 + 2] = (byte) ((intBits >> 16) & 0xFF);
            bytes[i * 4 + 3] = (byte) ((intBits >> 24) & 0xFF);
        }
        return bytes;
    }
}
