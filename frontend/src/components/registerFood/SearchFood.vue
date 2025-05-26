<!-- src/components/registerFood/FoodSearch.vue -->
<template>
  <div class="search-section">
    <h2>음식 검색</h2>
    <input v-model="query" placeholder="음식명을 입력하세요" class="search-input" />

    <ul class="search-result">
      <li v-for="food in paginatedFoods" :key="food.id" class="result-item" @click="select(food)">
        <span>{{ food.name }}</span>
        <small>{{ food.calories }} kcal</small>
      </li>
    </ul>

    <div class="pagination" v-if="totalPages > 1">
      <button v-for="page in totalPages" :key="page" class="page-button" :class="{ active: currentPage === page }" @click="setPage(page)">
        {{ page }}
      </button>
    </div>

    <!-- 로그인 필요 안내 -->
    <div v-if="error" class="error">{{ error }}</div>
  </div>
</template>

<script setup>
// Vue Composition API
import { ref, watch, computed } from "vue";
import { useRouter } from "vue-router";

// HTTP 클라이언트
import axios from "axios";
axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";
axios.defaults.withCredentials = true;

// 이벤트 방출
const emit = defineEmits(["select-food"]);

// 검색어, 목록, 페이징 상태
const query = ref("");
const foods = ref([]);
const error = ref("");
const loading = ref(false);

const currentPage = ref(1);
const itemsPerPage = 5;

// 서버에서 검색 결과 가져오기
let cancelToken;
watch(
  query,
  async (q) => {
    currentPage.value = 1;
    if (!q.trim()) {
      foods.value = [];
      return;
    }
    loading.value = true;
    error.value = "";
    // 이전 요청 취소
    if (cancelToken) cancelToken.cancel();
    cancelToken = axios.CancelToken.source();
    try {
      const res = await axios.get("/api/food/search", {
        params: { q: q.trim() },
        cancelToken: cancelToken.token,
      });
      foods.value = res.data; // [{ id, name, calories }, ...]
    } catch (e) {
      if (!axios.isCancel(e)) {
        console.error("검색 실패:", e);
        error.value = "검색 중 오류가 발생했습니다.";
      }
    } finally {
      loading.value = false;
    }
  },
  { debounce: 300 }
);

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

// 아이템 선택
function select(food) {
  emit("select-food", food);
}
</script>

<style scoped>
.search-section h2 {
  margin-bottom: 1rem;
}

.search-input {
  width: 100%;
  padding: 0.5rem;
  margin-bottom: 1.5rem;
  font-size: 1rem;
  border: 1px solid #ccc;
  border-radius: 6px;
}

.search-result {
  margin-bottom: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.result-item {
  background-color: #fff;
  padding: 0.75rem 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-item:hover {
  background-color: #ffe9b5;
}

.pagination {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 1rem;
}

.page-button {
  padding: 0.4rem 0.8rem;
  border: none;
  border-radius: 4px;
  background-color: #ffd983;
  cursor: pointer;
  font-weight: bold;
}

.page-button.active {
  background-color: #ffcc66;
  color: #333;
}

.error {
  color: #d32f2f;
  text-align: center;
  margin-top: 1rem;
}
</style>
