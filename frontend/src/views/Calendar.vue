<template>
  <BaseLayout>
    <div class="calendar-wrapper">
      <CalendarView
        :selected="selectedDate"
        :recorded-dates="Object.keys(mockData)"
        @select-date="handleDateSelect"
      />
      <div class="record-panel">
        <DailyRecord
          v-if="recordData"
          :data="recordData"
          :date="selectedDate"
        />
        <EmptyNotice v-else />
      </div>
    </div>
  </BaseLayout>
</template>

<script setup>
import { ref } from 'vue';
import CalendarView from '@/components/calendar/CalendarView.vue';
import DailyRecord from '@/components/calendar/DailyRecord.vue';
import EmptyNotice from '@/components/calendar/EmptyNotice.vue';

const today = new Date();
const selectedDate = ref(formatDate(today));
const recordData = ref(null);

// ✅ 추후 API 연결 지점
// ✅ 더미 데이터 (날짜: YYYY-MM-DD)
const mockData = {
  '2025-05-15': {
    diary: '오늘은 커피를 너무 많이 마셨다.',
    meals: {
      breakfast: '바나나',
      lunch: '김밥',
      snack: '아메리카노',
      dinner: '치킨',
    },
  },
  '2025-05-16': {
    diary: '점심엔 라면을 먹었다.',
    meals: {
      breakfast: '사과',
      lunch: '라면',
      dinner: '불고기',
    },
  },
};

function formatDate(date) {
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

function handleDateSelect(dateStr) {
  selectedDate.value = dateStr;
  recordData.value = mockData[dateStr] || null;
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.calendar-wrapper {
  display: flex;
  flex-direction: row;
  gap: clamp(2rem, 4vw, 6rem); /* ✅ 반응형 간격 */
  width: 100%;
  max-width: 1400px;
  padding: 1rem 2rem;
  box-sizing: border-box;
}

@media (max-width: 680px) {
  .calendar-wrapper {
    flex-direction: column;
    align-items: center;
    justify-content: center; /* ✅ 세로 정렬도 중앙 */
    padding: 1rem;
  }
}

.calendar-container,
.record-panel {
  flex: 1 1 500px;
  min-width: 360px;
  max-width: 600px;
  width: 100%;
}

.calendar-container {
  flex: 1.2;
  min-width: 350px;
  max-width: 800px;
  width: clamp(350px, 45vw, 700px); /* ✅ 반응형으로 커짐 */
  background-color: #fff8ed; /* ✅ 다시 명시 */
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.record-panel {
  flex: 1;
  min-width: 350px;
  max-width: 800px;
  width: clamp(350px, 45vw, 700px);
  padding: 2rem;
  font-size: 1.1rem;
}
</style>
