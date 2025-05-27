<template>
  <div>
    <div ref="target" class="record-container">
      <p class="record-title">{{ date }} 기록</p>

      <!-- 일기 섹션 -->
      <div v-if="data.diary" class="diary-section">
        <h3 class="section-title">📖 일기</h3>
        <div class="diary-content">
          {{ data.diary.content }}
        </div>
      </div>

      <!-- 식단 섹션 -->
      <div v-if="data.diets && data.diets.length > 0" class="diet-section">
        <h3 class="section-title">🍽️ 식단</h3>
        <div class="meal-groups">
          <div 
            v-for="diet in data.diets" 
            :key="diet.id" 
            class="meal-group"
          >
            <div class="meal-type">
              {{ getMealTypeLabel(diet.mealType) }}
            </div>
            <div class="food-list">
              <div 
                v-for="food in diet.foods" 
                :key="food.id"
                class="food-item"
              >
                <span class="food-name">{{ food.foodName }}</span>
                <span class="food-info">
                  {{ food.majorCategory }} | {{ food.kcal }}kcal
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 둘 다 없는 경우 -->
      <div v-if="!data.diary && (!data.diets || data.diets.length === 0)" class="no-data">
        이 날의 기록이 없습니다.
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, nextTick, ref } from 'vue';

const target = ref(null);

onMounted(async () => {
  await nextTick();
  if (target.value && target.value.parentNode) {
    console.log('[DailyRecord] 부모 요소 있음:', target.value.parentNode);
  }
});

const props = defineProps({
  data: {
    type: Object,
    required: true,
  },
  date: {
    type: String,
    required: true,
  },
});

function getMealTypeLabel(mealType) {
  const mealTypeMap = {
    'BREAKFAST': '아침',
    'LUNCH': '점심',
    'DINNER': '저녁',
    'SNACK': '간식',
  };
  return mealTypeMap[mealType] || mealType;
}
</script>

<style scoped>
.empty-notice {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  padding: 3rem;
  border-radius: 20px;
  text-align: center;
  color: #6b7280;
  font-size: 1.1rem;
  box-shadow: 0 8px 32px rgba(33, 213, 155, 0.15);
  min-height: 300px;
  border: 1px solid rgba(255, 255, 255, 0.2);

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1.5rem;
}

.empty-notice p {
  color: #2d5a52;
  font-weight: 500;
  font-size: 1.2rem;
}

.empty-notice button {
  padding: 1rem 2rem;
  border: none;
  border-radius: 12px;
  background: linear-gradient(90deg, #21d59b 0%, #ffc83d 100%);
  color: white;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 15px rgba(33, 213, 155, 0.3);
}

.empty-notice button:hover {
  background: linear-gradient(90deg, #1bc489 0%, #ffb84d 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(33, 213, 155, 0.4);
}
</style>
