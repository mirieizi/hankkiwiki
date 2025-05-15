<!-- src/views/Calendar.vue -->
<template>
  <div class="calendar-wrapper">
    <div class="calendar-panel">
      <CalendarView :selected="selectedDate" :recorded-dates="recordedDates" @select-date="handleDateSelect" />
    </div>
    <div class="record-panel">
      <DailyRecord v-if="hasRecord" :data="recordData" :date="selectedDate" />
      <EmptyNotice v-else :date="selectedDate" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from "vue";
import { useRoute } from "vue-router";
import axios from "axios";

import CalendarView from "@/components/calendar/CalendarView.vue";
import DailyRecord from "@/components/calendar/DailyRecord.vue";
import EmptyNotice from "@/components/calendar/EmptyNotice.vue";

// Axios 기본 설정 (main.js 에도 설정했으면 여기선 생략해도 됩니다)
axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";
axios.defaults.withCredentials = true;

// 날짜를 YYYY-MM-DD 포맷으로 변환
function formatDate(date) {
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
}

const route = useRoute();

// 선택된 날짜, 서버에서 가져온 해당 날짜의 데이터
const selectedDate = ref("");
const recordData = ref(null);

// 캘린더에 표시할 '기록된 날짜' 리스트
const recordedDates = ref([]);

// 기록이 있는지 여부
const hasRecord = computed(() => !!recordData.value);

// 특정 날짜의 기록을 서버에서 가져오는 함수
async function fetchRecord(dateStr) {
  try {
    const res = await axios.get("/api/diet/get-by-date", {
      params: { takeAt: dateStr },
    });
    recordData.value = res.data;
    // 기록된 날짜 배열에 추가 (중복 방지)
    if (!recordedDates.value.includes(dateStr)) {
      recordedDates.value.push(dateStr);
    }
  } catch (e) {
    // 404 등 데이터 없을 때
    recordData.value = null;
  }
}

// 날짜 선택 핸들러
function handleDateSelect(dateStr) {
  selectedDate.value = dateStr;
}

// 초기화: 라우터 쿼리 또는 오늘 날짜로 설정하고 fetch
onMounted(() => {
  const dateFromRoute = route.query.date;
  selectedDate.value = typeof dateFromRoute === "string" ? dateFromRoute : formatDate(new Date());
  fetchRecord(selectedDate.value);
});

// selectedDate가 바뀔 때마다 다시 fetch
watch(selectedDate, (newDate) => {
  fetchRecord(newDate);
});
</script>

<style scoped>
.calendar-wrapper {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: clamp(2rem, 4vw, 6rem);
  width: 100%;
  min-height: calc(100vh - 64px - 48px);
  padding: 2rem;
  box-sizing: border-box;
}

.calendar-panel,
.record-panel {
  flex: 1 1 360px;
  max-width: 700px;
  padding: 2rem;
}

.calendar-panel {
  border-radius: 12px;
  z-index: 1;
  background: #fff;
}

.record-panel {
  background: #fafafa;
  border-radius: 12px;
}

@media screen and (max-width: 480px) {
  .calendar-wrapper {
    flex-direction: column;
    padding: 1rem;
  }
  .calendar-panel,
  .record-panel {
    max-width: 100%;
    padding: 1rem;
  }
}
</style>
