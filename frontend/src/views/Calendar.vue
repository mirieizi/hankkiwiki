<!--scr/view/Calendar.vue-->
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
          v-if="hasRecord && recordData"
          :data="recordDataObject"
          :date="selectedDate"
        />
        <EmptyNotice v-else :date="selectedDate" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue';
import { useRoute } from 'vue-router';
import CalendarView from '@/components/calendar/CalendarView.vue';
import DailyRecord from '@/components/calendar/DailyRecord.vue';
import EmptyNotice from '@/components/calendar/EmptyNotice.vue';

function formatDate(date) {
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

function handleDateSelect(dateStr) {
  selectedDate.value = dateStr;
  const target = mockData[dateStr];
  recordData.value = target ? { ...target } : null;
}

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

// 변수
const selectedDate = ref('');
const recordData = ref(null);
const route = useRoute();

onMounted(() => {
  const dateFromRoute = route.query.date;
  selectedDate.value =
    typeof dateFromRoute === 'string' ? dateFromRoute : formatDate(new Date());

  recordData.value = mockData[selectedDate.value]
    ? { ...mockData[selectedDate.value] }
    : null;
});

watch(selectedDate, (newDate) => {
  console.log('[watch] selectedDate changed:', newDate);
  recordData.value = mockData[newDate] ? { ...mockData[newDate] } : null;
});

watch([recordData], () => {
  console.log('[debug] recordData:', recordData.value);
  console.log('[debug] hasRecord:', hasRecord.value); // ✅ 추가해줘!
});

const hasRecord = computed(
  () =>
    recordData.value !== null &&
    typeof recordData.value === 'object' &&
    recordData.value.meals &&
    Object.keys(recordData.value.meals).length > 0,
);

const recordDataObject = computed(() => recordData.value);
</script>

<style scoped>
.calendar-center-wrapper {
  display: grid;
  place-content: center;
  min-height: calc(100vh - 64px - 48px);
  transform: translateY(-5vh);
  width: 100%;
  min-width: 360px;
  max-width: 1100px;
  margin: 0 auto;
  padding: 2rem;
  box-sizing: border-box;
  padding: 2rem 3rem;
}

.calendar-wrapper {
  display: flex;
  flex-direction: row;
  gap: clamp(2rem, 4vw, 6rem); /* ✅ 반응형 간격 */
  max-width: none;
  flex-wrap: wrap;
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
  min-width: 360px;
  max-width: 800px;
  padding: 2rem;
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
