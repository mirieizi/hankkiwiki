<template>
  <div class="search-section">
    <h2>음식 검색</h2>
    <input v-model="query" placeholder="음식명을 입력하세요" class="search-input" @input="debouncedSearch" @keydown.enter="searchFoods" />

    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <span>검색 중...</span>
    </div>

    <div v-else-if="query && foods.length === 0 && !loading" class="no-results">검색 결과가 없습니다.</div>

    <ul v-else-if="foods.length > 0" class="search-result">
      <li v-for="food in paginatedFoods" :key="food.id" class="result-item" @click="select(food)">
        <div class="food-info">
          <span class="food-name">{{ food.foodName || food.name || "음식명 없음" }}</span>
          <small class="food-calories">{{ food.kcal || food.calories || 0 }} kcal</small>
          <small class="food-category">{{ food.majorCategory || food.category || "" }}</small>
        </div>
        <button class="add-button" @click.stop="select(food)">추가</button>
      </li>
    </ul>

    <div class="pagination" v-if="totalPages > 1">
      <button v-for="page in totalPages" :key="page" class="page-button" :class="{ active: currentPage === page }" @click="setPage(page)">
        {{ page }}
      </button>
    </div>

    <div v-if="error" class="error">
      {{ error }}
      <button @click="retrySearch" class="retry-button">다시 시도</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { toast } from "vue3-toastify";
import { foodService } from "@/services/foodService";

const emit = defineEmits(["select-food"]);

// 상태 관리
const query = ref("");
const foods = ref([]);
const error = ref("");
const loading = ref(false);
const currentPage = ref(1);
const itemsPerPage = 5;

// 디바운스 타이머
let searchTimer = null;

// 디바운스된 검색 함수
function debouncedSearch() {
  if (searchTimer) clearTimeout(searchTimer);

  searchTimer = setTimeout(async () => {
    if (!query.value.trim()) {
      foods.value = [];
      return;
    }

    await searchFoods();
  }, 300);
}

// 음식 검색 실행
async function searchFoods() {
  loading.value = true;
  error.value = "";
  currentPage.value = 1;

  try {
    console.log("검색어:", query.value.trim());
    const result = await foodService.searchFoods(query.value.trim());

    console.log("검색 결과 원본:", result);

    // 배열인지 확인하고 처리
    if (Array.isArray(result)) {
      foods.value = result;
    } else if (result && typeof result === "object") {
      // 단일 객체인 경우 배열로 변환
      foods.value = [result];
    } else {
      foods.value = [];
    }

    console.log("처리된 검색 결과:", foods.value);
  } catch (err) {
    console.error("검색 에러:", err);
    console.error("에러 응답:", err.response?.data);

    if (err.response?.status === 401) {
      toast.error("로그인이 필요합니다. 🔐");
    } else if (err.response?.status === 400) {
      toast.error("잘못된 검색 요청입니다. ⚠️");
    } else if (err.response?.status >= 500) {
      toast.error("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요. 🛠️");
    } else {
      toast.error("검색 중 오류가 발생했습니다. 😢");
    }

    foods.value = [];
  } finally {
    loading.value = false;
  }
}

// ✅ 검색 재시도 함수 유지
function retrySearch() {
  if (query.value.trim()) {
    console.log("검색 재시도:", query.value);
    searchFoods();
  } else {
    console.log("검색어가 없어서 재시도 불가");
  }
}

// 페이징 계산
const totalPages = computed(() => Math.ceil(foods.value.length / itemsPerPage));
const paginatedFoods = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  return foods.value.slice(start, start + itemsPerPage);
});

// 페이지 변경
function setPage(page) {
  currentPage.value = page;
}

// 음식 선택
function select(food) {
  emit("select-food", food);
}
</script>

<style scoped>
.search-section h2 {
  margin-bottom: 1.5rem;
  color: #2d5a52;
  font-size: 1.5rem;
  font-weight: 700;
  text-align: center;
}

.search-input {
  width: 100%;
  padding: 1rem;
  margin-bottom: 1.5rem;
  font-size: 1rem;
  border: 2px solid rgba(33, 213, 155, 0.2);
  border-radius: 12px;
  transition: all 0.2s ease;
  box-sizing: border-box;
  background: rgba(255, 255, 255, 0.8);
}

.search-input:focus {
  outline: none;
  border-color: #21d59b;
  background: white;
  box-shadow: 0 0 0 3px rgba(33, 213, 155, 0.1);
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 3rem;
  color: #2d5a52;
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 3px solid rgba(33, 213, 155, 0.3);
  border-top: 3px solid #21d59b;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.no-results {
  text-align: center;
  padding: 3rem;
  color: #6b7280;
  font-style: italic;
  background: rgba(248, 255, 252, 0.5);
  border-radius: 12px;
  border: 2px dashed rgba(33, 213, 155, 0.3);
}

.search-result {
  margin-bottom: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.result-item {
  background: rgba(248, 255, 252, 0.8);
  padding: 1rem;
  border: 1px solid rgba(33, 213, 155, 0.2);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-item:hover {
  background: rgba(230, 255, 250, 0.9);
  border-color: #21d59b;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(33, 213, 155, 0.2);
}

.food-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.food-name {
  font-weight: 600;
  color: #2d5a52;
}

.food-calories {
  color: #6b7280;
  font-size: 0.9rem;
}

.add-button {
  background: linear-gradient(90deg, #21d59b 0%, #1bc489 100%);
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(33, 213, 155, 0.3);
}

.add-button:hover {
  background: linear-gradient(90deg, #1bc489 0%, #17a673 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(33, 213, 155, 0.4);
}

.pagination {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 1rem;
}

.page-button {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  background-color: #f0f0f0;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.2s ease;
}

.page-button:hover {
  background-color: #ffd983;
}

.page-button.active {
  background-color: #ffcc66;
  color: #333;
}

.error {
  color: #d32f2f;
  text-align: center;
  padding: 1rem;
  background-color: #ffebee;
  border-radius: 6px;
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: center;
}

.retry-button {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  margin-top: 0.5rem;
  transition: background-color 0.2s ease;
}

.retry-button:hover {
  background-color: #d32f2f;
}
</style>
