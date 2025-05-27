<template>
  <div class="register-container">
    <h1 class="title">
      {{ isEditMode ? "식단 수정하기" : "오늘 뭐 먹음!" }}
    </h1>

    <div class="layout">
      <!-- 왼쪽: 음식 검색 + 선택 -->
      <div class="left-section">
        <SearchFood @select-food="addFood" />
        <SelectedFoodList :foods="selectedFoods" :selectedDate="selectedDate" :isEditMode="isEditMode" @remove-food="removeFood" @foods-saved="onFoodsSaved" @cancel-edit="cancelEdit" />
      </div>

      <!-- 오른쪽: 일기 작성 -->
      <div class="right-section">
        <DiaryEditor :date="selectedDate" :userName="userName" :isEditMode="isEditMode" @diary-saved="onDiarySaved" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watchEffect, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { foodService } from "@/services/foodService";
import { toast } from "vue3-toastify";
import SearchFood from "@/components/registerFood/SearchFood.vue";
import SelectedFoodList from "@/components/registerFood/SelectedFoodList.vue";
import DiaryEditor from "@/components/registerFood/DiaryEditor.vue";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const selectedDate = ref("");
const selectedFoods = ref([]);
const isEditMode = ref(false);

// 사용자 이름 computed (JWT에서 가져옴)
const userName = computed(() => {
  return userStore.userName || "사용자";
});

// 인증 확인 및 사용자 정보 로드
onMounted(async () => {
  const token = localStorage.getItem("accessToken");

  if (!token) {
    toast.error("로그인이 필요합니다. 🔐");
    setTimeout(() => router.push("/login"), 2000);
    return;
  }

  const isLoaded = userStore.loadUserFromToken();
  if (!isLoaded) {
    toast.error("인증 정보가 유효하지 않습니다. 다시 로그인해주세요. 🔑");
    setTimeout(() => router.push("/login"), 2000);
    return;
  }

  // ✅ edit 모드 확인
  isEditMode.value = route.query.edit === "true";
  console.log("수정 모드:", isEditMode.value);

  if (isEditMode.value) {
    console.log("수정 모드로 진입 - 기존 데이터 로드 시작");
    await loadExistingData();
  } else {
    console.log("일반 모드로 진입");
  }
});

// 날짜 감지: 쿼리에서 가져오되 없으면 오늘 날짜
watchEffect(() => {
  const dateFromRoute = route.query.date;
  if (typeof dateFromRoute === "string" && isValidDate(dateFromRoute)) {
    selectedDate.value = dateFromRoute;
  } else {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, "0");
    const dd = String(today.getDate()).padStart(2, "0");
    selectedDate.value = `${yyyy}-${mm}-${dd}`;
  }
});

async function loadExistingData() {
  console.log("=== 기존 데이터 로드 시작 ===");
  console.log("선택된 날짜:", selectedDate.value);

  try {
    const diets = await foodService.getDietsByDate(selectedDate.value);
    console.log("조회된 식단:", diets);

    if (diets && diets.length > 0) {
      const allFoods = [];

      for (const diet of diets) {
        if (diet.foods && diet.foods.length > 0) {
          console.log(`${diet.mealType} 식단:`, diet.foods);

          // ✅ 음식 데이터 구조 정규화
          const processedFoods = diet.foods.map((food) => ({
            id: food.id,
            foodName: food.foodName || food.name,
            kcal: food.kcal || food.calories || 0,
            majorCategory: food.majorCategory || food.category || "",
          }));

          allFoods.push(...processedFoods);
        }
      }

      selectedFoods.value = allFoods;
      console.log("로드된 음식들:", selectedFoods.value);

      if (allFoods.length > 0) {
        toast.success(`기존 식단 ${allFoods.length}개를 불러왔습니다! 📋`);
      }
    } else {
      console.log("해당 날짜에 식단 없음");
      selectedFoods.value = [];
      toast.info("해당 날짜에 저장된 식단이 없습니다. 새로 추가해보세요! 🍽️");
    }
  } catch (error) {
    console.error("기존 데이터 로드 실패:", error);
    selectedFoods.value = [];

    if (error.response?.status === 404) {
      toast.info("해당 날짜에 저장된 식단이 없습니다. 새로 추가해보세요! 🍽️");
    } else {
      toast.error("기존 데이터를 불러오는 중 오류가 발생했습니다. 😢");
    }
  }
}

// ✅ 수정 취소
function cancelEdit() {
  router.push(`/calendar?date=${selectedDate.value}`);
}

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
  if (!selectedFoods.value.find((f) => f.id === food.id)) {
    selectedFoods.value.push(food);
    toast.success(`${food.foodName || food.name}이(가) 추가되었습니다! 🍽️`);
  } else {
    toast.warning("이미 선택된 음식입니다. 🔄");
  }
}

// 음식 제거
function removeFood(food) {
  selectedFoods.value = selectedFoods.value.filter((f) => f.id !== food.id);
}

// 식단 저장 완료 후 처리
function onFoodsSaved(result) {
  console.log("식단 저장 완료:", result);

  // 선택된 음식 목록 초기화 (선택사항)
  selectedFoods.value = [];

  if (isEditMode.value) {
    router.push(`/calendar?date=${selectedDate.value}`);
  }
}

// 일기 저장 완료 후 처리
function onDiarySaved() {
  console.log("일기 저장 완료");

  // 저장 완료 후 달력 페이지로 이동하거나 다른 액션 수행
  // router.push('/calendar');
}
</script>

<style scoped>
.register-container {
  /* 전체 페이지 컨테이너 */
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #d9f6ee 0%, #c8f5ea 50%, #b8f4e6 100%);

  /* 중앙 정렬 */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;

  padding: 2rem;
  box-sizing: border-box;

  /* 스크롤 허용 */
  overflow-y: auto;
}

.title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 3rem;
  color: #2d5a52;
  text-align: center;
  background: linear-gradient(90deg, #21d59b 0%, #ffc83d 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

/* ====== 메인 레이아웃 컨테이너 ====== */
.layout {
  display: flex;
  flex-direction: row;
  gap: 3rem;
  justify-content: center;
  align-items: flex-start;
  width: 100%;
  max-width: 1400px;
  flex: 1;
}

/* ====== 공통 섹션 스타일 ====== */
.left-section,
.right-section {
  flex: 1 1 50%;
  min-width: 400px;
  max-width: 650px;

  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  padding: 2.5rem;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(33, 213, 155, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);

  box-sizing: border-box;
  max-height: 85vh; /* ✅ 최대 높이 제한 */
  overflow-y: auto; /* ✅ 개별 섹션 스크롤 */
  display: flex;
  flex-direction: column;
}

/* ✅ 스크롤바 스타일링 */
.left-section::-webkit-scrollbar,
.right-section::-webkit-scrollbar {
  width: 8px;
}

.left-section::-webkit-scrollbar-track,
.right-section::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.left-section::-webkit-scrollbar-thumb,
.right-section::-webkit-scrollbar-thumb {
  background: rgba(33, 213, 155, 0.3);
  border-radius: 4px;
}

.left-section::-webkit-scrollbar-thumb:hover,
.right-section::-webkit-scrollbar-thumb:hover {
  background: rgba(33, 213, 155, 0.5);
}

/* ====== 음식 검색 영역 (좌측) ====== */
.left-section {
  /* 추가 스타일링 없음 - 공통 스타일 사용 */
}

/* ====== 일기 작성 영역 (우측) ====== */
.right-section {
  /* 약간 다른 배경으로 구분 */
  background: rgba(255, 248, 225, 0.9);
}

/* ====== 대형 화면 (1200px 이상) ====== */
@media screen and (min-width: 1200px) {
  .layout {
    gap: 4rem;
  }

  .left-section,
  .right-section {
    min-width: 450px;
    max-width: 700px;
  }

  .register-container {
    padding: 3rem;
  }
}

/* ====== 중간 화면 (900px ~ 1199px) ====== */
@media screen and (max-width: 1199px) {
  .register-container {
    padding: 2rem;
  }

  .layout {
    gap: 2rem;
  }

  .left-section,
  .right-section {
    min-width: 350px;
    max-width: 550px;
    padding: 2rem;
  }

  .title {
    font-size: 2.2rem;
    margin-bottom: 2rem;
  }
}

/* ====== 태블릿 화면 (700px ~ 899px) ====== */
@media screen and (max-width: 899px) {
  .register-container {
    padding: 1.5rem;
  }

  .title {
    font-size: 2rem;
    margin-bottom: 1.5rem;
  }

  .layout {
    gap: 1.5rem;
  }

  .left-section,
  .right-section {
    min-width: 300px;
    max-width: 450px;
    padding: 1.5rem;
    min-height: 500px;
  }
}

/* ====== 모바일 화면 (700px 이하) - 세로 정렬 ====== */
@media screen and (max-width: 700px) {
  .layout {
    flex-direction: column;
    align-items: center;
    gap: 1.5rem;
  }

  .left-section,
  .right-section {
    flex: 1 1 auto;
    min-width: unset;
    max-width: 100%;
    width: 100%;
    min-height: 400px;
  }

  .title {
    font-size: 1.8rem;
    margin-bottom: 1rem;
  }

  .register-container {
    padding: 1rem;
  }
}

/* ====== 작은 모바일 (480px 이하) ====== */
@media screen and (max-width: 480px) {
  .register-container {
    padding: 0.75rem;
  }

  .left-section,
  .right-section {
    padding: 1rem;
    min-height: 350px;
  }

  .title {
    font-size: 1.5rem;
  }

  .layout {
    gap: 1rem;
  }
}

/* ====== 세로 화면 (모바일 회전) ====== */
@media screen and (max-height: 600px) and (orientation: landscape) {
  .register-container {
    min-height: calc(100vh - 50px - 40px);
    padding: 1rem;
  }

  .left-section,
  .right-section {
    min-height: 300px;
  }

  .title {
    margin-bottom: 1rem;
  }
}
</style>
