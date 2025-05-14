<template>
  <div class="search-section">
    <h2>음식 검색</h2>
    <input
      v-model="query"
      placeholder="음식명을 입력하세요"
      class="search-input"
    />

    <ul class="search-result">
      <li
        v-for="food in paginatedFoods"
        :key="food"
        class="result-item"
        @click="select(food)"
      >
        {{ food }}
      </li>
    </ul>

    <div class="pagination">
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
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue';

const props = defineProps({
  allFoods: {
    type: Array,
    required: true,
  },
});
const emit = defineEmits(['select-food']);

const query = ref('');
const currentPage = ref(1);
const itemsPerPage = 5;

const filteredFoods = computed(() => {
  return props.allFoods
    .filter((food) => food.includes(query.value.trim()))
    .sort((a, b) => a.localeCompare(b));
});

const totalPages = computed(() =>
  Math.ceil(filteredFoods.value.length / itemsPerPage),
);

const paginatedFoods = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  return filteredFoods.value.slice(start, start + itemsPerPage);
});

watch(query, () => {
  currentPage.value = 1;
});

function setPage(page) {
  currentPage.value = page;
}

function select(food) {
  emit('select-food', food);
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
  margin-bottom: 2rem;
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
  font-weight: 500;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-item:hover {
  background-color: #ffe9b5;
}

/* ====== 페이지네이션 (검색 결과 페이징 처리 UI) ====== */
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
</style>
