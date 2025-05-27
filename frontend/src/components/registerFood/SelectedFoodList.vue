<template>
  <div class="selected-foods">
    <h3>🍴 선택된 음식</h3>

    <div v-if="foods.length === 0" class="empty-state">
      <div class="empty-icon">🍽️</div>
      <p>아직 선택된 음식이 없습니다.</p>
      <small>위에서 음식을 검색하고 추가해보세요!</small>
    </div>

    <ul v-else class="selected-list">
      <li v-for="food in foods" :key="food.id" class="selected-item">
        <div class="food-info">
          <span class="food-name">{{ food.name }}</span>
          <small class="food-calories">{{ food.calories }} kcal</small>
        </div>
        <button class="remove-button" @click="removeFood(food)" aria-label="삭제">✕</button>
      </li>
    </ul>

    <div v-if="foods.length > 0" class="total-calories">
      <strong>총 칼로리: {{ totalCalories }} kcal</strong>
    </div>

    <!-- 식사 타입 선택 -->
    <div v-if="foods.length > 0" class="meal-type-section">
      <label for="mealType" class="meal-type-label">식사 타입:</label>
      <select v-model="selectedMealType" id="mealType" class="meal-type-select">
        <option value="BREAKFAST">아침</option>
        <option value="LUNCH">점심</option>
        <option value="DINNER">저녁</option>
        <option value="SNACK">간식</option>
      </select>
    </div>

    <button v-if="foods.length > 0" class="save-button" @click="saveDiet" :disabled="saving">
      {{ saving ? "저장 중..." : `식단 저장하기 (${foods.length}개)` }}
    </button>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { toast } from "vue3-toastify";
import { foodService } from "@/services/foodService";

const props = defineProps({
  foods: {
    type: Array,
    required: true,
  },
  selectedDate: {
    type: String,
    required: true,
  },
});

const emit = defineEmits(["remove-food", "foods-saved"]);

const saving = ref(false);
const selectedMealType = ref("LUNCH");

// 총 칼로리 계산
const totalCalories = computed(() => {
  return props.foods.reduce((sum, food) => sum + (food.calories || 0), 0);
});

// 음식 제거
function removeFood(food) {
  emit("remove-food", food);
  toast.info(`${food.foodName || food.name}이(가) 제거되었습니다.`);
}

// 식단 저장 (Diet API 사용)
async function saveDiet() {
  if (props.foods.length === 0) {
    toast.warning("선택된 음식이 없습니다. 🍽️");
    return;
  }

  saving.value = true;

  try {
    const dietData = {
      takeAt: props.selectedDate,
      mealType: selectedMealType.value,
      foods: props.foods.map((food) => ({
        foodId: food.id,
        name: food.foodName || food.name,
        calories: food.kcal || food.calories || 0,
      })),
    };

    console.log("저장할 식단 데이터:", dietData);
    const result = await foodService.createDiet(dietData);

    // ✅ 성공 토스트
    const mealTypeKorean =
      {
        BREAKFAST: "아침",
        LUNCH: "점심",
        DINNER: "저녁",
        SNACK: "간식",
      }[selectedMealType.value] || "식단";

    toast.success(`${mealTypeKorean} 식단이 저장되었습니다! 🎉`);
    emit("foods-saved", result);
  } catch (error) {
    console.error("식단 저장 실패:", error);

    if (error.response?.status === 401) {
      toast.error("로그인이 필요합니다. 🔐");
    } else if (error.response?.status === 400) {
      toast.error("잘못된 요청입니다. 다시 시도해주세요. ⚠️");
    } else if (error.response?.status >= 500) {
      toast.error("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요. 🛠️");
    } else {
      toast.error("식단 저장 중 오류가 발생했습니다. 😢");
    }
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
.selected-foods h3 {
  margin-bottom: 1.5rem;
  color: #333;
  font-size: 1.3rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.empty-state {
  text-align: center;
  padding: 3rem 1rem;
  color: #666;
  background-color: #f9f9f9;
  border-radius: 12px;
  border: 2px dashed #ddd;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.empty-state p {
  margin-bottom: 0.5rem;
  font-size: 1.1rem;
}

.empty-state small {
  color: #999;
}

.selected-list {
  margin-bottom: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.selected-item {
  background-color: #ffe9b5;
  padding: 1rem;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s ease;
  border: 1px solid #ffd983;
}

.selected-item:hover {
  background-color: #ffd983;
  transform: translateX(2px);
}

.food-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.food-name {
  font-weight: 500;
  color: #333;
}

.food-calories {
  color: #666;
  font-size: 0.9rem;
}

.remove-button {
  background: #ff6b6b;
  color: white;
  border: none;
  padding: 0.5rem;
  border-radius: 50%;
  cursor: pointer;
  font-size: 0.9rem;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.remove-button:hover {
  background: #ff5252;
  transform: scale(1.1);
}

.total-calories {
  text-align: center;
  padding: 1rem;
  background-color: #e8f5e8;
  border-radius: 8px;
  margin-bottom: 1rem;
  color: #2e7d32;
  border: 1px solid #c8e6c9;
}

.meal-type-section {
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.meal-type-label {
  font-weight: 500;
  color: #333;
}

.meal-type-select {
  flex: 1;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  background-color: white;
}

.save-button {
  background-color: #4caf50;
  color: white;
  border: none;
  padding: 1rem;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
  width: 100%;
  font-size: 1rem;
  position: relative;
}

.save-button:hover:not(:disabled) {
  background-color: #45a049;
  transform: translateY(-1px);
}

.save-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}
</style>
