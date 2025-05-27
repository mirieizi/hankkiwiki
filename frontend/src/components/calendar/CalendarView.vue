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
        <div v-if="hasRecord(date)" class="record-indicator">
          <span class="record-dot">●</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';

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

// emit 날짜 선택 및 월 변경
const emit = defineEmits(['select-date', 'month-change']);

function selectDate(date) {
  const formatted = formatDate(date);
  console.log('[CalendarView emit] 선택한 날짜:', formatted);
  emit('select-date', formatted);
}

// 월 변경 감지
watch([currentYear, currentMonth], ([year, month]) => {
  emit('month-change', year, month);
});

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
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  padding: 2.5rem;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(33, 213, 155, 0.15);
  box-sizing: border-box;
  width: 100%;
  min-width: 360px;
  max-width: 640px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 700;
  font-size: 1.4rem;
  margin-bottom: 1.5rem;
  color: #2d5a52;
}

.calendar-header button {
  background: linear-gradient(135deg, #21d59b 0%, #1bc489 100%);
  color: white;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0.7rem 1rem;
  border-radius: 12px;
  transition: all 0.2s ease;
  box-shadow: 0 4px 15px rgba(33, 213, 155, 0.3);
}

.calendar-header button:hover {
  background: linear-gradient(135deg, #1bc489 0%, #17a673 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(33, 213, 155, 0.4);
}

.calendar-today-button {
  text-align: center;
  margin-bottom: 1.5rem;
}

.calendar-today-button button {
  background: linear-gradient(90deg, #ffc83d 0%, #ffb84d 100%);
  border: none;
  padding: 0.7rem 1.5rem;
  border-radius: 25px;
  cursor: pointer;
  font-weight: 600;
  color: white;
  transition: all 0.2s ease;
  box-shadow: 0 4px 15px rgba(255, 200, 61, 0.3);
}

.calendar-today-button button:hover {
  background: linear-gradient(90deg, #ffb84d 0%, #ff9f5d 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 200, 61, 0.4);
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 0.75rem;
}

.day-label {
  text-align: center;
  font-weight: 700;
  color: #2d5a52;
  padding: 0.75rem 0;
  font-size: 1rem;
}

.day-cell {
  text-align: center;
  padding: 1rem 0;
  border-radius: 12px;
  cursor: pointer;
  background: linear-gradient(135deg, #f8fffc 0%, #f0fdf9 100%);
  transition: all 0.2s ease;
  position: relative;
  min-height: 3.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(33, 213, 155, 0.1);
}

.day-cell:hover {
  background: linear-gradient(135deg, #e6fffa 0%, #ccfff3 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(33, 213, 155, 0.2);
}

.day-cell.selected {
  background: linear-gradient(135deg, #21d59b 0%, #1bc489 100%);
  color: white;
  font-weight: bold;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(33, 213, 155, 0.4);
}

.day-cell.recorded {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  border: 2px solid #22c55e;
}

.day-cell.recorded.selected {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: white;
  border: 2px solid #15803d;
}

.day-cell.not-current-month {
  opacity: 0.3;
}

.day-cell.today {
  border: 2px solid #ffc83d;
  font-weight: bold;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
}

.day-number {
  font-size: 1rem;
  font-weight: 600;
}

.record-indicator {
  position: absolute;
  bottom: 4px;
  right: 4px;
}

.record-dot {
  font-size: 0.8rem;
  color: #22c55e;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
}

.day-cell.selected .record-dot {
  color: white;
}
</style>
