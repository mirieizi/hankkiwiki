<!-- src/views/Recommend.vue -->
<template>
  <div class="recommend-page">
    <!-- 실행 버튼 -->
    <div class="run-button">
      <RecommendButton :label="buttonLabel" :cost="spoonCost" :spoonCount="spoonCount" :loading="loading" :disabled="false" @run="onRun" />
    </div>

    <!-- 모드 선택 버튼 -->
    <div class="page-buttons">
      <router-link v-for="m in modes" :key="m.id" :to="`/recommend/${m.id}`" class="page-btn" :class="[m.id, { active: mode === m.id }]">
        <span class="btn-pill">{{ m.label }}</span>
        <span class="btn-icon">{{ m.icon }}</span>
      </router-link>
    </div>

    <!-- 전체 콘텐츠 영역 -->
    <div v-show="!showNoHistoryPrompt && !showLoginPrompt" class="content-area">
      <!-- 중앙 영역 -->
      <section class="main-view" :class="{ expanded }">
        <div v-if="!hasRun" class="placeholder">
          <img :src="placeholderImage" alt="추천 준비 중" />
          <p>버튼을 눌러 {{ modeLabel }} 받기</p>
        </div>
        <div v-if="hasRun" class="animation-wrapper">
          <component :is="animationComponent" :key="mode + '-' + runCount" @done="fetchRecommendation" />
        </div>
      </section>

      <!-- 결과 영역 -->
      <aside v-if="recommendation" class="result-box" :class="{ expanded }">
        <h3>{{ resultTitle }}</h3>
        <div class="food-card">
          <img :src="recommendation.image || placeholderImage" alt="추천 음식" />
          <p class="food-name">{{ recommendation.name }}</p>
          <p class="food-detail">{{ recommendation.detail }}</p>
        </div>
      </aside>
    </div>

    <!-- 로그인 안내 모달 -->
    <transition name="fade">
      <div v-if="showLoginPrompt" class="modal-overlay" @click.self="showLoginPrompt = false">
        <div class="modal" role="alertdialog">
          <p>로그인 후 이용이 가능합니다.</p>
          <div class="modal-buttons">
            <button class="modal-btn" @click="goToLogin">로그인하러 가기</button>
            <button class="modal-btn" @click="showLoginPrompt = false">닫기</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 기록 부족 모달 -->
    <transition name="fade">
      <div v-if="showNoHistoryPrompt" class="modal-overlay" @click.self="showNoHistoryPrompt = false">
        <div class="modal" role="alertdialog">
          <p>최근 3일간 식사 기록이 없습니다.</p>
          <div class="modal-buttons">
            <router-link to="/calendar" class="modal-btn">식사 입력하러 가기</router-link>
            <button class="modal-btn" @click="showNoHistoryPrompt = false">닫기</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
// Vue Composition API 불러오기
import { ref, computed, onMounted, watch, defineProps } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "axios";

// 컴포넌트 및 리소스 불러오기
import RecommendButton from "@/components/RecommendButton.vue";
import RandomAnimation from "@/components/RandomAnimation.vue";
import AnalysisAnimation from "@/components/AnalysisAnimation.vue";
import AIAnimation from "@/components/AIAnimation.vue";
import placeholderImage from "@/assets/eat_bear_logo.png";

// Axios 기본 설정
axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";
axios.defaults.withCredentials = true;

// props 및 라우터 준비
const props = defineProps({ initialMode: { type: String, default: null } });
const route = useRoute();
const router = useRouter();
const mode = ref(props.initialMode ?? route.params.mode ?? "random");

// 추천 모드 배열 정의
const modes = [
  { id: "random", label: "랜덤 추천", icon: "🎲", cost: 1, animation: RandomAnimation },
  { id: "history", label: "최근 3일 기반", icon: "🕓", cost: 1, animation: AnalysisAnimation },
  { id: "ai", label: "AI 추천", icon: "🤖", cost: 2, animation: AIAnimation },
];

// 상태 변수 정의
const remainingSpoons = ref(0); // 남은 추천 가능 횟수
const loading = ref(false); // 로딩 상태
const recommendation = ref(null); // 추천 결과
const hasRun = ref(false); // 추천 시도 플래그
const runCount = ref(0); // 애니메이션 키용 카운트
const showNoHistoryPrompt = ref(false); // 기록 부족 팝업
const showLoginPrompt = ref(false); // 로그인 안내 팝업
const historyRecords = ref([]); // 최근 기록 데이터

// 로그인 필요 시 팝업만 띄우는 함수
function requireLogin() {
  if (!localStorage.getItem("token")) {
    showLoginPrompt.value = true;
    return false;
  }
  return true;
}

// 남은 추천 호출 가능 횟수 API 조회
async function fetchRemainingSpoons() {
  try {
    const { data } = await axios.get("/api/recommend/remaining");
    remainingSpoons.value = data.count;
  } catch (e) {
    if (e.response?.status === 401) showLoginPrompt.value = true;
    else console.error("Remaining fetch 실패:", e);
  }
}

// 추천 생성 API 호출
async function fetchRecommendation() {
  try {
    const { data } = await axios.post("/api/recommend", null, { params: { mode: mode.value } });
    recommendation.value = data;
    await fetchRemainingSpoons();
  } catch (e) {
    if (e.response?.status === 401) showLoginPrompt.value = true;
    else console.error("Recommendation fetch 실패:", e);
  } finally {
    loading.value = false;
  }
}

// 로그인 페이지 이동
function goToLogin() {
  router.push("/login");
}

// 컴포넌트 마운트 시 초기 데이터 로드
onMounted(async () => {
  await fetchRemainingSpoons();

  // 최근 3일 기록 조회
  const today = new Date().toISOString().split("T")[0];
  const startDate = new Date(Date.now() - 2 * 864e5).toISOString().split("T")[0];
  try {
    const { data } = await axios.get("/api/diet", { params: { startDate, endDate: today } });
    historyRecords.value = data;
  } catch {
    historyRecords.value = [];
  }
});

// 라우터 모드 변경 감지
watch(
  () => route.params.mode,
  (m) => {
    if (!props.initialMode && m) {
      mode.value = m;
      recommendation.value = null;
      hasRun.value = false;
      runCount.value = 0;
      fetchRemainingSpoons();
    }
  }
);

// 계산된 속성 준비
const currentMode = computed(() => modes.find((m) => m.id === mode.value));
const spoonCost = computed(() => currentMode.value.cost);
const spoonCount = remainingSpoons; // 템플릿 호환 alias
const result = recommendation; // 템플릿 호환 alias
const buttonLabel = computed(() => currentMode.value.label);
const modeLabel = computed(() => currentMode.value.label);
const resultTitle = computed(() => `${modeLabel.value} 메뉴`);
const animationComponent = computed(() => currentMode.value.animation);
const expanded = computed(() => loading.value || !!recommendation.value);

// 실행 버튼 클릭 핸들러
function onRun() {
  if (!requireLogin()) return;
  if (remainingSpoons.value <= 0) {
    showNoHistoryPrompt.value = true;
    return;
  }
  if ((mode.value === "history" || mode.value === "ai") && historyRecords.value.length === 0) {
    showNoHistoryPrompt.value = true;
    return;
  }
  remainingSpoons.value -= spoonCost.value;
  hasRun.value = true;
  loading.value = true;
  runCount.value++;
}
</script>
<style scoped>
.recommend-page {
  display: grid;
  grid-template-columns: 0.5fr 1fr 1fr;
  grid-template-rows: auto auto auto;
  gap: 16px;
  padding: 16px;
}

.run-button {
  grid-column: 1 / span 3;
}

.page-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  border: none;
  border-radius: 24px;
  background: #eee;
  cursor: pointer;
  transition: background 0.2s;
}

.page-btn.active {
  background: #409eff;
  color: #fff;
}

.content-area {
  /* 1~3열 전부 덮어서 콘텐츠 최대화 */
  grid-column: 1 / span 3;
  grid-row: 2;
  display: grid;
  grid-template-columns: 0.5fr 1fr 1fr;
  align-items: start;
  gap: 16px;
  z-index: 1; /* 버튼보다 밑으로 */
}

/* 버튼 영역을 앞으로 띄우기 */
.page-buttons {
  grid-column: 1;
  grid-row: 2;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 2; /* 콘텐츠 위에 */
}

.main-view {
  grid-column: 2;
  background: transparent;
  border: 2px solid #000;
  border-radius: 12px;
  padding: 0;
  transition: border-color 0.3s ease;
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
}
/* 플레이스홀더 보여질 때(기본 상태), 완전히 가운데 정렬 */
.main-view:not(.expanded) {
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-view.expanded {
  border-color: #409eff;
}

.animation-wrapper {
  width: 100%;
  height: auto;
  overflow: visible;
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

.animation-wrapper :deep(img) {
  max-width: none;
  max-height: none;
}

.placeholder {
  grid-column: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  height: 100%;
}

.placeholder img {
  max-width: 120px;
  max-height: 120px;
  justify-content: center;
}

.result-box {
  grid-column: 3;
  background: transparent;
  padding: 16px;
  border-radius: 12px;
  box-shadow: none;
}

.food-card {
  text-align: center;
}

.food-card img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  object-fit: cover;
  margin-bottom: 8px;
}

.food-name {
  font-size: 18px;
  font-weight: bold;
}

.food-detail {
  font-size: 14px;
  color: #666;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  text-align: center;
}

.modal-buttons {
  display: flex;
  gap: 8px;
  margin-top: 16px;
}

.modal-btn {
  padding: 8px 16px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

@media (max-width: 768px) {
  .recommend-page {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto auto auto;
  }
  .page-buttons {
    flex-direction: row;
    overflow-x: auto;
  }
  .content-area {
    grid-template-columns: 1fr;
  }
  .result-box {
    grid-column: 1;
    margin-top: 16px;
  }
}
</style>
