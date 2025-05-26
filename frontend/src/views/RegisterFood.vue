<template>
  <div class="register-container">
    <h1 class="title">오늘 뭐 먹음!</h1>

    <div class="layout">
      <!-- 왼쪽: 음식 검색 + 선택 -->
      <div class="left-section">
        <SearchFood @select-food="addFood" />
        <SelectedFoodList 
          :foods="selectedFoods" 
          :selectedDate="selectedDate"
          @remove-food="removeFood"
          @foods-saved="onFoodsSaved"
        />
      </div>

      <!-- 오른쪽: 일기 작성 -->
      <div class="right-section">
        <DiaryEditor
          :date="selectedDate"
          :userName="currentUser.name || '사용자'"
          @diary-saved="onDiarySaved"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watchEffect, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import SearchFood from '@/components/registerFood/SearchFood.vue';
import SelectedFoodList from '@/components/registerFood/SelectedFoodList.vue';
import DiaryEditor from '@/components/registerFood/DiaryEditor.vue';

const route = useRoute();
const router = useRouter();
const selectedDate = ref('');
const selectedFoods = ref([]);
const currentUser = ref({
  name: '양미이' // 실제로는 JWT에서 사용자 정보를 가져와야 함
});

// 인증 확인
onMounted(() => {
  const token = localStorage.getItem('accessToken');
  if (!token) {
    alert('로그인이 필요합니다.');
    router.push('/login');
    return;
  }
});

// 날짜 감지: 쿼리에서 가져오되 없으면 오늘 날짜
watchEffect(() => {
  const dateFromRoute = route.query.date;
  if (typeof dateFromRoute === 'string' && isValidDate(dateFromRoute)) {
    selectedDate.value = dateFromRoute;
  } else {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    selectedDate.value = `${yyyy}-${mm}-${dd}`;
  }
});

// 날짜 유효성 검사
function isValidDate(dateString) {
  const regex = /^\d{4}-\d{2}-\d{2}$/;
  if (!regex.test(dateString)) return false;
  
  const date = new Date(dateString);
  return date instanceof Date && !isNaN(date.getTime());
}

// 음식 추가
function addFood(food) {
  // 중복 확인 (ID 기준)
  if (!selectedFoods.value.find(f => f.id === food.id)) {
    selectedFoods.value.push(food);
  } else {
    alert('이미 선택된 음식입니다.');
  }
}

// 음식 제거
function removeFood(food) {
  selectedFoods.value = selectedFoods.value.filter(f => f.id !== food.id);
}

// 식단 저장 완료 후 처리
function onFoodsSaved(result) {
  console.log('식단 저장 완료:', result);
  
  // 선택된 음식 목록 초기화 (선택사항)
  selectedFoods.value = [];
}

// 일기 저장 완료 후 처리
function onDiarySaved() {
  console.log('일기 저장 완료');
  
  // 저장 완료 후 달력 페이지로 이동하거나 다른 액션 수행
  // router.push('/calendar');
}
</script>

<style scoped>
.register-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
  width: 100%;
}

.title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 2rem;
  text-align: center;
  color: #333;
}

/* ====== 메인 레이아웃 컨테이너 ====== */
.layout {
  display: flex;
  flex-direction: row;
  gap: 2rem;
  justify-content: space-between;
  align-items: flex-start;
  width: 100%;
}

/* ====== 공통 섹션 스타일 ====== */
.left-section,
.right-section {
  flex: 1 1 50%;
  min-width: 350px;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
  min-height: 600px;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  position: relative;
  overflow: hidden;
}

/* ====== 음식 검색 영역 (좌측) ====== */
.left-section {
  background-color: #f8f8f8;
}

/* ====== 일기 작성 영역 (우측) ====== */
.right-section {
  background-color: #fff8e1;
}

/* ====== 대형 화면 (1200px 이상) ====== */
@media screen and (min-width: 1200px) {
  .layout {
    gap: 3rem;
  }
  
  .left-section,
  .right-section {
    min-width: 400px;
  }
}

/* ====== 중간 화면 (900px ~ 1199px) ====== */
@media screen and (max-width: 1199px) {
  .register-container {
    max-width: 100%;
    padding: 1.5rem;
  }
  
  .layout {
    gap: 1.5rem;
  }
  
  .left-section,
  .right-section {
    min-width: 320px;
    padding: 1.5rem;
  }
}

/* ====== 태블릿 화면 (700px ~ 899px) ====== */
@media screen and (max-width: 899px) {
  .register-container {
    padding: 1rem;
  }
  
  .title {
    font-size: 1.75rem;
    margin-bottom: 1.5rem;
  }
  
  .layout {
    gap: 1rem;
  }
  
  .left-section,
  .right-section {
    min-width: 280px;
    padding: 1rem;
    min-height: 500px;
  }
}

/* ====== 모바일 화면 (700px 이하) - 세로 정렬 ====== */
@media screen and (max-width: 700px) {
  .layout {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .left-section,
  .right-section {
    flex: 1 1 auto;
    min-width: unset;
    width: 100%;
    min-height: 400px;
  }
  
  .title {
    font-size: 1.5rem;
    margin-bottom: 1rem;
  }
  
  .register-container {
    padding: 0.75rem;
  }
}

/* ====== 작은 모바일 (480px 이하) ====== */
@media screen and (max-width: 480px) {
  .register-container {
    padding: 0.5rem;
  }
  
  .left-section,
  .right-section {
    padding: 0.75rem;
    min-height: 350px;
  }
  
  .title {
    font-size: 1.25rem;
  }
}
</style>
