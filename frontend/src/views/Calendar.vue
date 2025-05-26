<template>
  <div class="calendar-page">
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
.calendar-page {
  /* 전체 페이지 컨테이너 */
  min-height: calc(100vh - 64px - 60px);
  width: 100%;
  background: linear-gradient(135deg, #d9f6ee 0%, #c8f5ea 50%, #b8f4e6 100%);
  
  /* 중앙 정렬 */
  display: flex;
  justify-content: center;
  align-items: center;
  
  padding: 2rem;
  box-sizing: border-box;
}

.calendar-wrapper {
  /* 내부 컨테이너 */
  display: flex;
  flex-direction: row;
  gap: 3rem;
  
  /* 최대 너비 제한 */
  max-width: 1200px;
  width: 100%;
  
  /* 내용물 정렬 */
  justify-content: center;
  align-items: flex-start;
}

.calendar-panel,
.record-panel {
  /* 기본 flex 설정 */
  flex: 1 1 450px; /* 최소 450px에서 동일하게 확장 */
  min-width: 350px;
  max-width: 600px;
  
  /* 높이 설정 */
  min-height: 500px;
  height: auto;
  
  /* 기타 */
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

/* 큰 화면 (1200px 이상) */
@media screen and (min-width: 1200px) {
  .calendar-wrapper {
    gap: 4rem;
    padding: 3rem;
  }
  
  .calendar-panel,
  .record-panel {
    flex: 1 1 500px;
    max-width: 650px;
  }
}

/* 중간 화면 (768px ~ 1199px) */
@media screen and (max-width: 1199px) {
  .calendar-wrapper {
    gap: 2rem;
    padding: 1.5rem;
  }
  
  .calendar-panel,
  .record-panel {
    flex: 1 1 400px;
    min-width: 320px;
    max-width: 550px;
  }
}

/* 태블릿 (600px ~ 767px) - 아직 가로 배치 유지 */
@media screen and (max-width: 767px) {
  .calendar-wrapper {
    gap: 1.5rem;
    padding: 1rem;
    min-height: calc(100vh - 64px - 60px);
  }
  
  .calendar-panel,
  .record-panel {
    flex: 1 1 300px;
    min-width: 280px;
    max-width: 400px;
  }
}

/* 작은 화면 (600px 이하) - 세로 정렬로 전환 */
@media screen and (max-width: 600px) {
  .calendar-wrapper {
    /* 세로 정렬로 변경 */
    flex-direction: column;
    align-items: stretch;
    justify-content: flex-start;
    
    /* 패딩 조정 */
    padding: 1rem;
    gap: 1rem;
    
    /* 높이 자동 조정 */
    min-height: auto;
  }
  
  .calendar-panel,
  .record-panel {
    /* 전체 너비 사용 */
    flex: 1 1 auto;
    min-width: unset;
    max-width: 100%;
    width: 100%;
    
    /* 높이 조정 */
    min-height: 400px;
  }
}

/* 매우 작은 화면 (480px 이하) */
@media screen and (max-width: 480px) {
  .calendar-wrapper {
    padding: 0.5rem;
    gap: 0.75rem;
  }
  
  .calendar-panel,
  .record-panel {
    min-height: 350px;
  }
}

/* sidebar가 있는 경우 (왼쪽 여백 고려) */
@media screen and (min-width: 1024px) {
  .calendar-wrapper {
    /* sidebar 너비만큼 왼쪽 마진 추가 (예: 240px) */
    margin-left: 0; /* sidebar가 fixed가 아니라면 0으로 유지 */
    
    /* 또는 sidebar가 fixed라면: */
    /* margin-left: 240px; */
  }
}

/* 세로 화면 (모바일 회전) */
@media screen and (max-height: 600px) and (orientation: landscape) {
  .calendar-wrapper {
    min-height: calc(100vh - 50px - 40px); /* 더 작은 header/footer */
    padding: 1rem;
  }
  
  .calendar-panel,
  .record-panel {
    min-height: 300px;
  }
}
</style>
