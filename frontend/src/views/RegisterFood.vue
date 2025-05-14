// ✅ RegisterFood.vue (페이지 조립 뷰)
<template>
  <div class="register-container">
    <h1 class="title">오늘 뭐 먹음!</h1>

    <div class="layout">
      <!-- 왼쪽: 음식 검색 + 선택 -->
      <div class="left-section">
        <SearchFood :allFoods="allFoods" @select-food="addFood" />
        <SelectedFoodList :foods="selectedFoods" @remove-food="removeFood" />
      </div>

      <!-- 오른쪽: 일기 작성 -->
      <div class="right-section">
        <DiaryEditor :date="today" userName="양미이" @submit="submitDiary" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import SearchFood from '@/components/registerFood/SearchFood.vue';
import SelectedFoodList from '@/components/registerFood/SelectedFoodList.vue';
import DiaryEditor from '@/components/registerFood/DiaryEditor.vue';

// 💡 더미 음식 데이터 (사전 연결 전)
const allFoods = ref([
  '닭볶음탕',
  '된장찌개',
  '김치찌개',
  '비빔밥',
  '불고기',
  '제육볶음',
  '갈비탕',
  '순두부찌개',
  '떡볶이',
  '잡채',
]);

const diaryContent = ref('');
const selectedFoods = ref([]);
const today = new Date();

function addFood(food) {
  if (!selectedFoods.value.includes(food)) {
    selectedFoods.value.push(food);
  }
}

function removeFood(food) {
  selectedFoods.value = selectedFoods.value.filter((f) => f !== food);
}

function submitDiary() {
  console.log('일기 내용:', diaryContent.value);
}
</script>

<style scoped>
.register-container {
  padding: 2rem;
  max-width: 1600px;
  margin: auto;
}

.title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 2rem;
  text-align: center;
}

.layout {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 3rem;
  justify-content: center;
  align-items: flex-start;
}

.left-section,
.right-section {
  flex: 1 1 600px; /* 가로 너비 기준 설정 */
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
  min-height: 600px;
  height: auto;
  position: relative;
  overflow: hidden;
}

/* ================= 공통 Diary Section 컨테이너 스타일 ================= */
.right-section {
  background-color: #fff8e1;
  max-width: none; /* ✅ 기존 600px → 제거 */
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  min-height: 800px;
  height: auto;
  position: relative;
  padding: 2rem;
  overflow: hidden; /* 버튼 잘림 방지 & 하단 끊기 */
  border-radius: 12px;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 10rem; /* footer와 여백 확보 */
  box-sizing: border-box;
  isolation: isolate;
  z-index: 1;
}

/* ====== 음식 검색 영역 (좌측: 음식 검색 및 선택 박스) ====== */
.left-section {
  background-color: #f8f8f8;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  min-height: 800px;
  height: auto;
  position: relative;
  padding-bottom: 5rem;
  overflow: hidden;
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 8rem;
  box-sizing: border-box;
  isolation: isolate;
  z-index: 1;
}

/* ====== 반응형: 화면 좁을 경우 세로 정렬로 전환 ====== */
@media screen and (max-width: 680px) {
  .layout {
    flex-direction: column;
    align-items: stretch;
  }
  .left-section,
  .right-section {
    flex: 1 1 100%;
  }
}
</style>
