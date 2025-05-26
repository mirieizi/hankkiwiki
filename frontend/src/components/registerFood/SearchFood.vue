<template>
  <div class="search-section">
    <h2>음식 검색</h2>
    <input 
      v-model="query" 
      placeholder="음식명을 입력하세요" 
      class="search-input"
      @input="debouncedSearch"
    />

    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <span>검색 중...</span>
    </div>

    <div v-else-if="query && foods.length === 0 && !loading" class="no-results">
      검색 결과가 없습니다.
    </div>

    <ul v-else-if="foods.length > 0" class="search-result">
      <li 
        v-for="food in paginatedFoods" 
        :key="food.id" 
        class="result-item" 
        @click="select(food)"
      >
        <div class="food-info">
          <span class="food-name">{{ food.name }}</span>
          <small class="food-calories">{{ food.calories }} kcal</small>
        </div>
        <button class="add-button" @click.stop="select(food)">
          추가
        </button>
      </li>
    </ul>

    <div class="pagination" v-if="totalPages > 1">
      <button 
        v-for="page in totalPages" 
        :key="page" 
        class="page-button" 
        :class="{ active: currentPage === page }" 
        @click="setPage(page)"
      >
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
import { ref, computed } from 'vue';
import { foodService } from '@/services/foodService';

const emit = defineEmits(['select-food']);

// 상태 관리
const query = ref('');
const foods = ref([]);
const error = ref('');
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
  error.value = '';
  currentPage.value = 1;

  try {
    const result = await foodService.searchFoods(query.value.trim());
    foods.value = Array.isArray(result) ? result : [];
  } catch (err) {
    console.error('검색 에러:', err);
    
    if (err.response?.status === 401) {
      error.value = '로그인이 필요합니다.';
    } else if (err.response?.status >= 500) {
      error.value = '서버 오류가 발생했습니다.';
    } else {
      error.value = '검색 중 오류가 발생했습니다.';
    }
    
    foods.value = [];
  } finally {
    loading.value = false;
  }
}

// 검색 재시도
function retrySearch() {
  if (query.value.trim()) {
    searchFoods();
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
  emit('select-food', food);
}
</script>

<style scoped>
.search-section h2 {
  margin-bottom: 1rem;
  color: #333;
  font-size: 1.5rem;
}

.search-input {
  width: 100%;
  padding: 0.75rem;
  margin-bottom: 1.5rem;
  font-size: 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  transition: border-color 0.2s ease;
  box-sizing: border-box;
}

.search-input:focus {
  outline: none;
  border-color: #ffd983;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 2rem;
  color: #666;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #ffd983;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.no-results {
  text-align: center;
  padding: 2rem;
  color: #666;
  font-style: italic;
}

.search-result {
  margin-bottom: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.result-item {
  background-color: #fff;
  padding: 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-item:hover {
  background-color: #ffe9b5;
  border-color: #ffd983;
  transform: translateY(-1px);
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

.add-button {
  background-color: #4caf50;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.add-button:hover {
  background-color: #45a049;
  transform: scale(1.05);
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
  transition: background-color 0.2s ease;
}

.retry-button:hover {
  background-color: #d32f2f;
}
</style>
