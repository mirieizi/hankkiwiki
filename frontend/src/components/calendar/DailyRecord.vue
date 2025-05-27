<template>
  <div>
    <div ref="target" class="record-container">
      <div class="record-header">
        <p class="record-title">{{ date }} 기록</p>
        <button @click="goToEdit" class="edit-button">✏️ 수정하기</button>
      </div>

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
          <div v-for="diet in data.diets" :key="diet.id" class="meal-group">
            <div class="meal-type">
              {{ getMealTypeLabel(diet.mealType) }}
            </div>
            <div class="food-list">
              <div v-for="food in diet.foods" :key="food.id" class="food-item">
                <span class="food-name">{{ food.foodName }}</span>
                <span class="food-info">{{ food.majorCategory }} | {{ food.kcal }}kcal</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 둘 다 없는 경우 -->
      <div v-if="!data.diary && (!data.diets || data.diets.length === 0)" class="no-data">이 날의 기록이 없습니다.</div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, nextTick, ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const target = ref(null);

onMounted(async () => {
  await nextTick();
  if (target.value && target.value.parentNode) {
    console.log("[DailyRecord] 부모 요소 있음:", target.value.parentNode);
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

// 수정 페이지로 이동
function goToEdit() {
  router.push(`/register-food?date=${props.date}&edit=true`);
}

function getMealTypeLabel(mealType) {
  const mealTypeMap = {
    BREAKFAST: "아침",
    LUNCH: "점심",
    DINNER: "저녁",
    SNACK: "간식",
  };
  return mealTypeMap[mealType] || mealType;
}
</script>

<style scoped>
.record-container {
  width: 100%;
  max-width: 700px;
  min-height: 300px;
  padding: 2.5rem;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(33, 213, 155, 0.15);
  color: #2d5a52;
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.record-container:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(33, 213, 155, 0.2);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.record-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2d5a52;
  background: linear-gradient(90deg, #21d59b 0%, #ffc83d 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
}

.edit-button {
  background: linear-gradient(90deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.9rem;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
}

.edit-button:hover {
  background: linear-gradient(90deg, #2563eb 0%, #1d4ed8 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.section-title {
  font-size: 1.2rem;
  font-weight: 700;
  margin-bottom: 1rem;
  color: #2d5a52;
  border-bottom: 2px solid rgba(33, 213, 155, 0.2);
  padding-bottom: 0.5rem;
}

/* 일기 섹션 */
.diary-section {
  margin-bottom: 2rem;
}

.diary-content {
  background: linear-gradient(135deg, #f8fffc 0%, #f0fdf9 100%);
  padding: 1.5rem;
  border-radius: 12px;
  line-height: 1.7;
  color: #374151;
  font-style: italic;
  border: 1px solid rgba(33, 213, 155, 0.1);
  box-shadow: 0 2px 8px rgba(33, 213, 155, 0.05);
}

/* 식단 섹션 */
.diet-section {
  margin-bottom: 2rem;
}

.meal-groups {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.meal-group {
  border: 1px solid rgba(33, 213, 155, 0.2);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(33, 213, 155, 0.1);
  transition: all 0.2s ease;
}

.meal-group:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(33, 213, 155, 0.15);
}

.meal-type {
  background: linear-gradient(90deg, #21d59b 0%, #1bc489 100%);
  padding: 1rem 1.5rem;
  font-weight: 700;
  color: white;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.food-list {
  padding: 1.5rem;
  background: rgba(248, 255, 252, 0.5);
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.food-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  border: 1px solid rgba(33, 213, 155, 0.1);
  transition: all 0.2s ease;
}

.food-item:hover {
  background: rgba(230, 255, 250, 0.9);
  transform: translateX(2px);
  box-shadow: 0 2px 8px rgba(33, 213, 155, 0.1);
}

.food-name {
  font-weight: 600;
  color: #2d5a52;
}

.food-info {
  font-size: 0.9rem;
  color: #6b7280;
}

/* 데이터 없음 */
.no-data {
  text-align: center;
  color: #999;
  font-style: italic;
  padding: 2rem;
  background-color: rgba(248, 255, 252, 0.5);
  border-radius: 8px;
  border: 2px dashed rgba(33, 213, 155, 0.3);
}

/* 반응형 */
@media (max-width: 640px) {
  .record-container {
    padding: 1.5rem;
  }

  .record-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .edit-button {
    width: 100%;
    text-align: center;
  }

  .food-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .meal-groups {
    gap: 0.75rem;
  }
}
</style>
