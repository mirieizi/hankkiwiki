<template>
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

// emit 날짜 선택
const emit = defineEmits(['select-date']);

function formatDate(date) {
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

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
  width: 400px;
}
.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  margin-bottom: 0.5rem;
}
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}
.day-label {
  text-align: center;
  font-weight: bold;
  color: #888;
}

/* 선택된 일자 색상, 기록된 일자 색상 지정 */
.day-cell {
  text-align: center;
  padding: 0.8rem 0;
  border-radius: 50%;
  cursor: pointer;
  background-color: #f8f9fa;
  position: relative;
}
.day-cell.not-current-month {
  color: #ccc;
}
.day-cell.recorded {
  background-color: #ffe2b3;
}
.day-cell.selected {
  background-color: #ffc085 !important; /* 진한 오렌지 */
  font-weight: bold;
  color: #5c3b1e;
}
.day-cell.selected .day-number::after {
  content: '';
  display: block;
  margin: 2px auto 0;
  width: 40%;
  height: 2px;
  background-color: #5c3b1e;
  border-radius: 1px;
}

/* 오늘 일자로 이동하기 버튼 */
.calendar-today-button {
  display: flex;
  justify-content: center;
  margin: 0.5rem 0 1rem 0;
}
.calendar-today-button button {
  background-color: #339af0;
  color: white;
  padding: 6px 16px;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
}
.calendar-today-button button:hover {
  background-color: #1c7ed6;
}
</style>
