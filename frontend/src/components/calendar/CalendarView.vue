<template>
  <!-- 전체 달력을 카드 형태로 감쌈 -->
  <div class="calendar-view">
    <div class="calendar-header">
      <button @click="prevMonth">‹</button>
      <span>{{ currentYear }}년 {{ currentMonth + 1 }}월</span>
      <button @click="nextMonth">›</button>
    </div>

    <div class="calendar-today-button">
      <button @click="goToToday">오늘</button>
    </div>

    <div class="calendar-grid">
      <div class="day-label" v-for="day in dayLabels" :key="day">{{ day }}</div>
      <div
        v-for="(date, index) in calendarDates"
        :key="index"
        class="day-cell"
        :class="{
          today: isToday(date),
          selected: isSelected(date),
          recorded: hasRecord(date),
          'not-current-month': date.getMonth() !== currentMonth,
        }"
        @click="selectDate(date)"
      >
        <span class="day-number">{{ date.getDate() }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const today = new Date();
const currentYear = ref(today.getFullYear());
const currentMonth = ref(today.getMonth());

const dayLabels = ['월', '화', '수', '목', '금', '토', '일'];

// 날짜 계산
function getStartDate(year, month) {
  const first = new Date(year, month, 1);
  const day = first.getDay() === 0 ? 7 : first.getDay();
  return new Date(year, month, 1 - (day - 1));
}

const calendarDates = computed(() => {
  const dates = [];
  const start = getStartDate(currentYear.value, currentMonth.value);
  for (let i = 0; i < 42; i++) {
    const date = new Date(start);
    date.setDate(start.getDate() + i);
    dates.push(date);
  }
  return dates;
});

function prevMonth() {
  if (currentMonth.value === 0) {
    currentMonth.value = 11;
    currentYear.value--;
  } else {
    currentMonth.value--;
  }
}
function nextMonth() {
  if (currentMonth.value === 11) {
    currentMonth.value = 0;
    currentYear.value++;
  } else {
    currentMonth.value++;
  }
}

function formatDate(date) {
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

// emit 날짜 선택
const emit = defineEmits(['select-date']);

function selectDate(date) {
  const formatted = formatDate(date);
  console.log('[CalendarView emit] 선택한 날짜:', formatted); // 🔍 로그 확인
  emit('select-date', formatted);
}

function isToday(date) {
  const now = new Date();
  return (
    date.getDate() === now.getDate() &&
    date.getMonth() === now.getMonth() &&
    date.getFullYear() === now.getFullYear()
  );
}

// 오늘로 이동 기능
function goToToday() {
  currentYear.value = today.getFullYear();
  currentMonth.value = today.getMonth();
  emit('select-date', formatDate(today));
}

// 색상 관련
const props = defineProps({
  selected: String,
  recordedDates: {
    type: Array,
    default: () => [],
  },
});

function isSelected(date) {
  return formatDate(date) === props.selected;
}

function hasRecord(date) {
  return props.recordedDates.includes(formatDate(date));
}
</script>

<style scoped>
.calendar-view {
  background-color: #ffffff;
  padding: 2rem;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  box-sizing: border-box;
  width: 100%;
  min-width: 360px;
  max-width: 640px;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 1.25rem;
  margin-bottom: 1rem;
}

.calendar-today-button {
  text-align: center;
  margin-bottom: 1rem;
}

.calendar-today-button button {
  background-color: #ffe2b3;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  cursor: pointer;
  font-weight: 500;
  transition: 0.2s;
}
.calendar-today-button button:hover {
  background-color: #ffc085;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 0.5rem;
}

.day-label {
  text-align: center;
  font-weight: bold;
  color: #666;
}

.day-cell {
  text-align: center;
  padding: 0.75rem 0;
  border-radius: 12px;
  cursor: pointer;
  background-color: #fff9f0;
  transition: 0.2s;
}

.day-cell:hover {
  background-color: #ffe2b3;
}

.day-cell.selected {
  background-color: #ffc085;
  color: white;
  font-weight: bold;
}

.day-cell.recorded {
  border-bottom: 2px solid chocolate;
}

.day-cell.not-current-month {
  opacity: 0.3;
}

.day-cell.today {
  border: 2px solid #ffc085;
}
</style>
