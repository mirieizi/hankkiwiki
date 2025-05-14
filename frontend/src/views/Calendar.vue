<template>
  <div class="calendar-center-wrapper">
    <div class="calendar-wrapper">
      <div class="calendar-panel">
        <CalendarView
          :selected="selectedDate"
          :recorded-dates="Object.keys(mockData)"
          @select-date="handleDateSelect"
        />
      </div>
      <div class="record-panel">
        <DailyRecord
          v-if="recordData"
          :data="recordData"
          :date="selectedDate"
        />
        <EmptyNotice v-else date="selectedDate" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watchEffect } from 'vue';
import { useRoute } from 'vue-router';
import CalendarView from '@/components/calendar/CalendarView.vue';
import DailyRecord from '@/components/calendar/DailyRecord.vue';
import EmptyNotice from '@/components/calendar/EmptyNotice.vue';

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

// 변수수
const today = new Date();
const selectedDate = ref(formatDate(today));
const recordData = ref({});
const route = useRoute();

watchEffect(() => {
  const dateFromRoute = route.query.date;
  if (typeof dateFromRoute === 'string') {
    selectedDate.value = dateFromRoute;
  } else {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    selectedDate.value = `${yyyy}-${mm}-${dd}`;
  }

  recordData.value = mockData[selectedDate.value] || null;
});
</script>

<style scoped>
.calendar-center-wrapper {
  display: grid;
  place-content: center;
  min-height: calc(100vh - 64px - 48px); /* 헤더, 푸터 높이 제외 */
  transform: translateY(-5vh);
  width: 100%;
  min-width: 360px;
  max-width: 1100px;
  margin: 0 auto; /* ✅ 가운데 정렬 */
  padding: 2rem; /* ✅ 양 옆에 여백 */
  box-sizing: border-box;
  padding: 2rem 3rem; /* ✅ 사이드바 침범 방지 */
}

.calendar-wrapper {
  display: flex;
  flex-direction: row;
  gap: clamp(2rem, 4vw, 6rem); /* ✅ 반응형 간격 */
  max-width: none;
  padding: 1rem 2rem;
  justify-content: center;
  align-items: flex-start;
}

@media (max-width: 680px) {
  .calendar-wrapper,
  .record-panel {
    flex-direction: column;
    align-items: center;
    justify-content: center; /* ✅ 세로 정렬도 중앙 */
    padding: 1rem;
  }
}

.calendar-panel,
.record-panel {
  flex: 1 1 500px;
  min-width: 360px;
  max-width: 600px;
  width: 100%;
}

.calendar-panel {
  width: 100%;
  display: flex;
  height: auto;
  border-radius: 12px;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
  isolation: isolate;
  z-index: 1;
  justify-content: flex-start;
}

.record-panel {
  flex: 1;
  min-width: 350px;
  max-width: 800px;
  width: clamp(350px, 45vw, 700px);
  padding: 2rem;
  font-size: 1.1rem;
}
@media screen and (max-width: 480px) {
  .layout {
    flex-direction: column;
    align-items: stretch;
  }
  .calendar-panel,
  .record-panel {
    flex: 1 1 100%;
  }
}
</style>
