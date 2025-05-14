<!-- components/calendar/DailyRecord.vue -->
<template>
  <div class="record-container">
    <p class="record-title">{{ date }} 식사 기록</p>

    <p class="diary">{{ data.diary }}</p>

    <div class="meal-list">
      <div class="meal-item" v-for="(value, key) in data.meals" :key="key">
        <span class="meal-time">{{ convertMealKey(key) }}</span>
        <span class="meal-menu">{{ value }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  data: {
    type: Object,
    required: true,
  },
  date: {
    type: String,
    default: '',
  },
})

function convertMealKey(key) {
  const map = {
    breakfast: '아침',
    lunch: '점심',
    snack: '간식',
    dinner: '저녁',
  }
  return map[key] || key
}
</script>

<style scoped>
.record-container {
  background-color: #fff;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  min-height: 300px;
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
