-- ${HANKKI_DATABASE}와 맞춰야 함
USE hankki;

-- 유저별 식단 검색을 위한 복합 인덱스
CREATE INDEX idx_diet_user_takeat ON diet(user_id, take_at);

-- diet_meal_item 조인을 위한 인덱스
CREATE INDEX idx_diet_meal_dietid ON diet_meal_item(diet_id);