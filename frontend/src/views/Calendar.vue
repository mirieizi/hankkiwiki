<template>
  <div class="calendar-wrapper">
    <div class="calendar-panel">
      <CalendarView 
        :selected="selectedDate" 
        :recorded-dates="recordedDates" 
        @select-date="handleDateSelect"
        @month-change="handleMonthChange"
      />
    </div>
    <div class="record-panel">
      <DailyRecord 
        v-if="hasRecord" 
        :data="recordData" 
        :date="selectedDate" 
      />
      <EmptyNotice 
        v-else 
        :date="selectedDate" 
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from "vue";
import { useRoute } from "vue-router";
import { calendarService } from "@/services/calendarService";

import CalendarView from "@/components/calendar/CalendarView.vue";
import DailyRecord from "@/components/calendar/DailyRecord.vue";
import EmptyNotice from "@/components/calendar/EmptyNotice.vue";

// 날짜를 YYYY-MM-DD 포맷으로 변환
function formatDate(date) {
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
}

const route = useRoute();

// 상태 관리
const selectedDate = ref("");
const recordData = ref(null);
const recordedDates = ref([]);
const loading = ref(false);
const currentYear = ref(new Date().getFullYear());
const currentMonth = ref(new Date().getMonth());

// 기록이 있는지 여부
const hasRecord = computed(() => {
  return recordData.value && (
    (recordData.value.diets && recordData.value.diets.length > 0) ||
    recordData.value.diary
  );
});

// 특정 날짜의 기록을 서버에서 가져오는 함수
async function fetchRecord(dateStr) {
  if (!dateStr) return;
  
  loading.value = true;
  try {
    const dayRecord = await calendarService.getDayRecord(dateStr);
    recordData.value = dayRecord;
    
    // 기록된 날짜 배열에 추가 (중복 방지)
    if (dayRecord.hasRecord && !recordedDates.value.includes(dateStr)) {
      recordedDates.value.push(dateStr);
    }
  } catch (error) {
    console.error('기록 조회 실패:', error);
    recordData.value = {
      date: dateStr,
      diets: [],
      diary: null,
      hasRecord: false
    };
  } finally {
    loading.value = false;
  }
}

// 월별 기록된 날짜들 가져오기
async function fetchRecordedDatesInMonth(year, month) {
  try {
    const dates = await calendarService.getRecordedDatesInMonth(year, month);
    recordedDates.value = dates;
  } catch (error) {
    console.error('월별 기록 날짜 조회 실패:', error);
    recordedDates.value = [];
  }
}

// 캘린더 뷰에서 월 변경 감지를 위한 이벤트
function handleMonthChange(year, month) {
  currentYear.value = year;
  currentMonth.value = month;
  fetchRecordedDatesInMonth(year, month);
}

// 날짜 선택 핸들러
function handleDateSelect(dateStr) {
  selectedDate.value = dateStr;
}

// 캘린더 뷰에서 월 변경 감지를 위한 이벤트
function handleMonthChange(year, month) {
  currentYear.value = year;
  currentMonth.value = month;
  fetchRecordedDatesInMonth(year, month);
}

// 초기화
onMounted(async () => {
  const dateFromRoute = route.query.date;
  const today = formatDate(new Date());
  
  selectedDate.value = typeof dateFromRoute === "string" ? dateFromRoute : today;
  
  // 현재 월의 기록된 날짜들 먼저 조회
  await fetchRecordedDatesInMonth(currentYear.value, currentMonth.value);
  
  // 선택된 날짜의 상세 기록 조회
  await fetchRecord(selectedDate.value);
});

// selectedDate가 바뀔 때마다 다시 fetch
watch(selectedDate, (newDate) => {
  if (newDate) {
    fetchRecord(newDate);
  }
});

// 라우트 쿼리 변경 감지
watch(
  () => route.query.date,
  (newDate) => {
    if (newDate && typeof newDate === 'string') {
      selectedDate.value = newDate;
    }
  }
);
</script>

<style scoped>
.calendar-wrapper {
  display: flex;
  flex-direction: row;
  place-content: center;
  gap: clamp(2rem, 4vw, 6rem);
  width: 100%;
  max-width: none;
  min-height: calc(100vh - 64px - 48px);
  transform: translateY(-5vh);
  flex-wrap: wrap;
  padding: 2rem;
  align-items: flex-start;
  box-sizing: border-box;
}

.calendar-panel,
.record-panel {
  display: block;
  flex: 1 1 500px;
  min-width: 360px;
  max-width: 700px;
  width: 100%;
}

.calendar-panel {
  height: auto;
  border-radius: 12px;
  box-sizing: border-box;
  isolation: isolate;
  z-index: 1;
  padding: 2rem;
}

.record-panel {
  flex: 1;
  min-width: 360px;
  max-width: 800px;
  padding: 2rem;
}

@media screen and (max-width: 480px) {
  .calendar-wrapper {
    flex-direction: column;
    align-items: center;
    padding: 1rem;
  }
  .calendar-panel,
  .record-panel {
    flex: 1 1 100%;
    max-width: 100%;
    justify-content: center;
  }
}
</style>
