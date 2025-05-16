<!-- components/calendar/DailyRecord.vue -->
<template>
  <div>
    <div ref="target" class="record-container">
      <p class="record-title">{{ date }} 식사 기록</p>

      <p class="diary" v-if="data">{{ data.diary }}</p>

      <div class="meal-list" v-if="data?.meals">
        <div class="meal-item" v-for="(value, key) in data.meals" :key="key">
          <span class="meal-time">{{ convertMealKey(key) }}</span>
          <span class="meal-menu">{{ value }}</span>
        </div>
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

function convertMealKey(key) {
  const map = {
    breakfast: '아침',
    lunch: '점심',
    snack: '간식',
    dinner: '저녁',
  };
  return map[key] || key;
}
</script>

<style scoped>
.record-container {
  width: 100%; /* ✅ 부모 영역 꽉 채움 */
  max-width: 700px; /* ✅ 최대 폭 제한 (디자인 목적) */
  min-height: 300px; /* ✅ 내용물이 적어도 일정 높이 확보 */
  padding: 2rem;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  color: black;
}
.record-title {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 1rem;
}
.diary {
  font-style: italic;
  color: #444;
  margin-bottom: 1rem;
}
.meal-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.meal-item {
  display: flex;
  justify-content: space-between;
  padding: 0.6rem 1rem;
  background-color: #f8f9fa;
  border-radius: 8px;
}
.meal-time {
  font-weight: bold;
  color: #333;
}
.meal-menu {
  color: #666;
}
</style>
