package com.hankki.common.upload;
//@Service
//@RequiredArgsConstructor
//@Slf4j
public class CSVDataLoader {

//    private final RedisCommands<String, byte[]> redisCommands; // 타입 일치
//
//    private FoodEmbedding processEmbeddingRow(String[] row, int rowNum) {
//        try {
//            String foodName = safeGetString(row, 0, "");
//            String gender = safeGetString(row, 1, "unknown").toLowerCase();
//
//            // Food ID 조회
//            Optional<Food> foodOpt = foodRepository.findByFoodName(foodName);
//            if (foodOpt.isEmpty()) {
//                log.warn("Food 없음 (row={}): {}", rowNum, foodName);
//                return null;
//            }
//
//            Long foodId = foodOpt.get().getId();
//
//            // Redis 키 생성
//            String redisKey;
//            if ("female".equals(gender)) {
//                redisKey = "food_female:" + foodId;
//            } else if ("male".equals(gender)) {
//                redisKey = "food_male:" + foodId;
//            } else {
//                redisKey = "food_unknown:" + foodId;
//            }
//
//            // 벡터 데이터 추출
//            byte[] vectorBytes = toLittleEndianFloatBytes(row, 2, detectedVectorDimension);
//
//            // Redis 저장 및 확인
//            try {
//                redisCommands.set(redisKey, vectorBytes);
//
//                // 저장 확인
//                byte[] stored = redisCommands.get(redisKey);
//                if (stored == null) {
//                    log.error("Redis 저장 실패: {}", redisKey);
//                    return null;
//                } else {
//                    log.debug("Redis 저장 성공: {} ({}bytes)", redisKey, stored.length);
//                }
//
//            } catch (Exception e) {
//                log.error("Redis 저장 중 오류: {}", redisKey, e);
//                return null;
//            }
//
//            // 메타 정보 생성
//            return FoodEmbedding.builder()
//                    .food(foodOpt.get())
//                    .gender(FoodEmbedding.Gender.fromString(gender))
//                    .embeddingKey(redisKey)
//                    .dimensionCount(detectedVectorDimension)
//                    .checksum(String.valueOf(Arrays.hashCode(row)))
//                    .build();
//
//        } catch (Exception e) {
//            log.error("Embedding row 처리 실패 (row={}): {}", rowNum, e.getMessage());
//            return null;
//        }
//    }
}
