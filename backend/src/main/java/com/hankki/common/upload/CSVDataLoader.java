package com.hankki.common.upload;

import com.hankki.domain.food.repository.FoodRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.hankki.domain.food.entity.Food;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class CSVDataLoader {

//    private final FoodRepository foodRepository;
//    private final FoodEmbeddingRepository foodEmbeddingRepository;
//    private final RedisCommands<String, byte[]> redisCommands;
//    private final EntityManager em;
//
//    @Value("${data.sync.use-full-data:false}")
//    private boolean useFullData;
//
//    @Value("${data.sync.batch-size:1000}")
//    private int batchSize;
//
//    private int detectedVectorDimension = -1;
//
//    // --- 1) Food 데이터 로드 ---
//    public void loadFoodDataFromCsv() {
//        String path = useFullData ? "vectors/food_data.csv" : "vectors/mini_food_data.csv";
//
//        try (CSVReader reader = new CSVReader(new InputStreamReader(
//                new ClassPathResource(path).getInputStream()))) {
//
//            List<String[]> rows = reader.readAll();
//            if (rows.size() < 2) {
//                log.warn("CSV에 데이터가 없습니다: {}", path);
//                return;
//            }
//
//            // 기존 데이터 체크
//            long existingCount = foodRepository.count();
//            if (existingCount > 0) {
//                log.info("Food 테이블에 이미 {}개 데이터가 있어 로드를 건너뜁니다.", existingCount);
//                return;
//            }
//
//            // 헤더 로깅
//            String[] headers = rows.get(0);
//            log.info("Food CSV 헤더: {}", Arrays.toString(headers));
//
//            List<String[]> dataRows = rows.subList(1, rows.size());
//            List<Food> batch = new ArrayList<>(batchSize);
//            AtomicInteger processed = new AtomicInteger(0);
//            AtomicInteger failed = new AtomicInteger(0);
//
//            for (int i = 0; i < dataRows.size(); i++) {
//                String[] row = dataRows.get(i);
//                try {
//                    Food food = parseFood(row, i + 2); // row number for logging
//                    if (food != null) {
//                        batch.add(food);
//                        processed.incrementAndGet();
//                    }
//                } catch (Exception e) {
//                    failed.incrementAndGet();
//                    log.warn("Food CSV 파싱 실패 (row={}): {}", i + 2, e.getMessage());
//                }
//
//                // 배치 저장
//                if (batch.size() >= batchSize || i == dataRows.size() - 1) {
//                    if (!batch.isEmpty()) {
//                        saveFoodBatch(batch);
//                        log.info("Food 배치 저장 완료: {}/{} (실패: {})",
//                                processed.get(), dataRows.size(), failed.get());
//                    }
//                    batch.clear();
//                    em.clear(); // 메모리 정리
//                }
//            }
//
//            log.info("Food 데이터 로딩 완료 - 성공: {}, 실패: {}", processed.get(), failed.get());
//
//        } catch (IOException | CsvException e) {
//            log.error("Food CSV 읽기 실패: {}", path, e);
//            throw new RuntimeException("Food 데이터 로딩 실패", e);
//        }
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    protected void saveFoodBatch(List<Food> batch) {
//        try {
//            foodRepository.saveAll(batch);
//            foodRepository.flush();
//        } catch (Exception e) {
//            log.error("Food 배치 저장 실패, 개별 저장 시도", e);
//            // 개별 저장 시도
//            for (Food food : batch) {
//                try {
//                    foodRepository.save(food);
//                } catch (Exception ex) {
//                    log.warn("개별 Food 저장 실패: {}", food.getFoodName(), ex);
//                }
//            }
//        }
//    }
//
//    private Food parseFood(String[] row, int rowNum) {
//        try {
//            if (row.length < 12) {
//                log.warn("Food CSV 컬럼 부족 (row={}): 필요=12, 실제={}", rowNum, row.length);
//                return null;
//            }
//
//            return Food.builder()
//                    .foodName(safeGetString(row, 0, "Unknown_" + rowNum))
//                    .majorCategory(safeGetString(row, 1, "기타"))
//                    .subCategory(safeGetString(row, 2, "기타"))
//                    .servingSize(safeGetDouble(row, 3, 100.0))
//                    .kcal(safeGetDouble(row, 4, 0.0))
//                    .moisture(safeGetDouble(row, 5, 0.0))
//                    .carbohydrate(safeGetDouble(row, 6, 0.0))
//                    .protein(safeGetDouble(row, 7, 0.0))
//                    .fat(safeGetDouble(row, 8, 0.0))
//                    .sugar(safeGetDouble(row, 9, 0.0))
//                    .sodium(safeGetDouble(row, 10, 0.0))
//                    .cholesterol(safeGetDouble(row, 11, 0.0))
//                    .build();
//        } catch (Exception e) {
//            log.error("Food 파싱 실패 (row={}): {}", rowNum, e.getMessage());
//            return null;
//        }
//    }
//
//    // --- 2) Embedding 로드 ---
//    public void loadEmbeddingsFromCsv() {
//        String path = useFullData ? "vectors/food_embeddings.csv" : "vectors/mini_food_embeddings.csv";
//
//        try (CSVReader reader = new CSVReader(new InputStreamReader(
//                new ClassPathResource(path).getInputStream()))) {
//
//            List<String[]> rows = reader.readAll();
//            if (rows.size() < 2) {
//                log.warn("Embedding CSV에 데이터가 없습니다: {}", path);
//                return;
//            }
//
//            // 기존 데이터 체크
//            long existingCount = foodEmbeddingRepository.count();
//            if (existingCount > 0) {
//                log.info("FoodEmbedding 테이블에 이미 {}개 데이터가 있어 로드를 건너뜁니다.", existingCount);
//                return;
//            }
//
//            // 벡터 차원 감지
//            String[] headers = rows.get(0);
//            detectedVectorDimension = headers.length - 2; // food_name, gender 제외
//            log.info("Embedding CSV 헤더: {}", Arrays.toString(headers));
//            log.info("감지된 벡터 차원: {}", detectedVectorDimension);
//
//            List<String[]> dataRows = rows.subList(1, rows.size());
//            List<FoodEmbedding> batch = new ArrayList<>(batchSize);
//            AtomicInteger processed = new AtomicInteger(0);
//            AtomicInteger failed = new AtomicInteger(0);
//
//            // Redis 연결 테스트
//            try {
//                redisCommands.ping();
//                log.info("Redis 연결 확인 완료");
//            } catch (Exception e) {
//                log.error("Redis 연결 실패", e);
//                throw new RuntimeException("Redis 연결 실패", e);
//            }
//
//            for (int i = 0; i < dataRows.size(); i++) {
//                String[] row = dataRows.get(i);
//                try {
//                    FoodEmbedding embedding = processEmbeddingRow(row, i + 2);
//                    if (embedding != null) {
//                        batch.add(embedding);
//                        processed.incrementAndGet();
//                    }
//                } catch (Exception e) {
//                    failed.incrementAndGet();
//                    log.warn("Embedding 처리 실패 (row={}): {}", i + 2, e.getMessage());
//                }
//
//                // 배치 저장
//                if (batch.size() >= batchSize || i == dataRows.size() - 1) {
//                    if (!batch.isEmpty()) {
//                        saveEmbeddingBatch(batch);
//                        log.info("Embedding 배치 저장 완료: {}/{} (실패: {})",
//                                processed.get(), dataRows.size(), failed.get());
//                    }
//                    batch.clear();
//                    em.clear();
//                }
//            }
//
//            log.info("Embedding 데이터 로딩 완료 - 성공: {}, 실패: {}", processed.get(), failed.get());
//
//        } catch (IOException | CsvException e) {
//            log.error("Embedding CSV 읽기 실패: {}", path, e);
//            throw new RuntimeException("Embedding 데이터 로딩 실패", e);
//        }
//    }
//
//    private FoodEmbedding processEmbeddingRow(String[] row, int rowNum) {
//        try {
//            if (row.length < 3) {
//                log.warn("Embedding CSV 컬럼 부족 (row={}): 최소=3, 실제={}", rowNum, row.length);
//                return null;
//            }
//
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
//                log.warn("알 수 없는 gender (row={}): {}", rowNum, gender);
//            }
//
//            // 벡터 데이터 추출 및 Redis 저장
//            byte[] vectorBytes = toLittleEndianFloatBytes(row, 2, detectedVectorDimension);
//            redisCommands.set(redisKey, vectorBytes);
//
//            // 메타 정보 생성
//            return FoodEmbedding.builder()
//                    .foodId(foodId)
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
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    protected void saveEmbeddingBatch(List<FoodEmbedding> batch) {
//        try {
//            foodEmbeddingRepository.saveAll(batch);
//            foodEmbeddingRepository.flush();
//        } catch (Exception e) {
//            log.error("Embedding 배치 저장 실패", e);
//            throw e;
//        }
//    }
//
//    /** CSV의 특정 구간을 Little Endian Float32 byte[]로 변환 */
//    private byte[] toLittleEndianFloatBytes(String[] row, int offset, int dimension) {
//        ByteBuffer buffer = ByteBuffer.allocate(dimension * Float.BYTES)
//                .order(ByteOrder.LITTLE_ENDIAN);
//
//        for (int i = 0; i < dimension; i++) {
//            try {
//                if (offset + i < row.length) {
//                    float value = Float.parseFloat(row[offset + i].trim());
//                    buffer.putFloat(value);
//                } else {
//                    buffer.putFloat(0.0f); // 기본값
//                }
//            } catch (NumberFormatException e) {
//                log.warn("벡터 값 파싱 실패, 0으로 대체: {}", row[offset + i]);
//                buffer.putFloat(0.0f);
//            }
//        }
//
//        return buffer.array();
//    }
//
//    public int getDetectedVectorDimension() {
//        return detectedVectorDimension;
//    }
//
//    // Helper methods
//    private String safeGetString(String[] row, int index, String defaultValue) {
//        return (index < row.length && row[index] != null && !row[index].trim().isEmpty())
//                ? row[index].trim() : defaultValue;
//    }
//
//    private Double safeGetDouble(String[] row, int index, Double defaultValue) {
//        try {
//            return (index < row.length && !row[index].trim().isEmpty()) ?
//                    Double.parseDouble(row[index].trim()) : defaultValue;
//        } catch (NumberFormatException e) {
//            log.warn("숫자 파싱 실패 (index={}): {}, 기본값 사용: {}", index, row[index], defaultValue);
//            return defaultValue;
//        }
//    }
}

