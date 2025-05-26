USE hankki;

-- 기존 diet 관련 인덱스들 (원래 있던 것들)
CREATE INDEX IF NOT EXISTS idx_diet_user_takeat ON diet(user_id, take_at);
CREATE INDEX IF NOT EXISTS idx_diet_meal_dietid ON diet_meal_item(diet_id);

-- food 테이블 최적화 인덱스들
CREATE UNIQUE INDEX IF NOT EXISTS idx_food_name_unique
    ON food(food_name);

CREATE INDEX IF NOT EXISTS idx_food_major_category
    ON food(major_category);

CREATE INDEX IF NOT EXISTS idx_food_sub_category
    ON food(sub_category);
    
ALTER TABLE food ADD FULLTEXT INDEX IF NOT EXISTS ft_food_name (food_name);

-- 복합 인덱스 (카테고리 조합 검색용)
CREATE INDEX IF NOT EXISTS idx_food_categories
    ON food(major_category, sub_category);

-- 영양소 검색용 인덱스들
CREATE INDEX IF NOT EXISTS idx_food_kcal
    ON food(kcal);

CREATE INDEX IF NOT EXISTS idx_food_protein
    ON food(protein);

CREATE INDEX IF NOT EXISTS idx_food_carbohydrate
    ON food(carbohydrate);

CREATE INDEX IF NOT EXISTS idx_food_fat
    ON food(fat);

-- 영양소 범위 검색용 복합 인덱스 (고단백 저칼로리 등)
CREATE INDEX IF NOT EXISTS idx_food_nutrition_combo
    ON food(kcal, protein, carbohydrate, fat);

-- 서빙 사이즈 검색용
CREATE INDEX IF NOT EXISTS idx_food_serving_size
    ON food(serving_size);

-- food_embedding 메타 테이블
CREATE TABLE IF NOT EXISTS food_embedding (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              food_id BIGINT NOT NULL,
                                              embedding_key VARCHAR(255) NOT NULL UNIQUE,
    dimension_count INT NOT NULL DEFAULT 9, -- 실제 데이터는 PC1~PC9 (9차원)
    gender ENUM('male', 'female', 'unknown') DEFAULT 'unknown',
    checksum VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (food_id) REFERENCES food(id) ON DELETE CASCADE,

    -- 인덱스들
    INDEX idx_embedding_food_id (food_id),
    INDEX idx_embedding_key (embedding_key),
    INDEX idx_embedding_gender (gender),

    -- 복합 인덱스 (food_id + gender 조합으로 검색)
    UNIQUE INDEX idx_food_gender_unique (food_id, gender)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 동기화 상태 관리용
CREATE TABLE IF NOT EXISTS data_sync_status (
                                                id INT AUTO_INCREMENT PRIMARY KEY,
                                                sync_type VARCHAR(50) NOT NULL,
    total_records BIGINT DEFAULT 0,
    synced_records BIGINT DEFAULT 0,
    last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    sync_status ENUM('PENDING','IN_PROGRESS','COMPLETED','FAILED') DEFAULT 'PENDING',
    error_message TEXT,
    progress_percentage DECIMAL(5,2) DEFAULT 0.00,

    UNIQUE KEY unique_sync_type (sync_type),
    INDEX idx_sync_status (sync_status),
    INDEX idx_last_sync (last_sync_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 동기화 상태 초기 데이터
INSERT INTO data_sync_status(sync_type, sync_status, total_records)
VALUES
    ('FOOD_DATA','PENDING', 0),
    ('REDIS_VECTORS','PENDING', 0)
    ON DUPLICATE KEY UPDATE
                         sync_status = 'PENDING',
                         synced_records = 0,
                         progress_percentage = 0.00,
                         error_message = NULL;

-- Redis 성능 최적화를 위한 추가 설정값 저장
CREATE TABLE IF NOT EXISTS system_config (
                                             id INT AUTO_INCREMENT PRIMARY KEY,
                                             config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO system_config(config_key, config_value, description)
VALUES
    ('vector_dimension', '9', '실제 벡터 차원 수 (PC1~PC9)'),
    ('batch_size', '1000', '데이터 동기화 배치 크기'),
    ('redis_key_prefix_male', 'food_male:', '남성용 Redis 키 접두사'),
    ('redis_key_prefix_female', 'food_female:', '여성용 Redis 키 접두사')
    ON DUPLICATE KEY UPDATE
                         config_value = VALUES(config_value),
                         description = VALUES(description);

-- 성능 분석용 쿼리 실행 계획 확인 (주석 처리)
-- EXPLAIN SELECT * FROM food WHERE major_category = '음료류' AND sub_category = '커피류';
-- EXPLAIN SELECT * FROM food WHERE kcal BETWEEN 100 AND 200 AND protein > 10;
-- EXPLAIN SELECT f.*, fe.embedding_key FROM food f JOIN food_embedding fe ON f.id = fe.food_id WHERE f.food_name = '사과';
