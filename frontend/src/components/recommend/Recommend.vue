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
          <component :is="animationComponent" :key="mode + '-' + runCount" @done="() => fetchRecommendation(mode)" />
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
      <div v-if="showLoginPrompt" class="modal-overlay" @click.self="hideLogin">
        <div class="modal" role="alertdialog">
          <p>로그인 후 이용이 가능합니다.</p>
          <div class="modal-buttons">
            <button class="modal-btn" @click="goToLogin">로그인하러 가기</button>
            <button class="modal-btn" @click="hideLogin">닫기</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 기록 부족 모달 -->
    <transition name="fade">
      <div v-if="showNoHistoryPrompt" class="modal-overlay" @click.self="hideNoHistory">
        <div class="modal" role="alertdialog">
          <p>최근 3일간 식사 기록이 없습니다.</p>
          <div class="modal-buttons">
            <router-link to="/calendar" class="modal-btn">식사 입력하러 가기</router-link>
            <button class="modal-btn" @click="hideNoHistory">닫기</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, defineProps } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useRecommendStore } from "@/stores/recommend";
import RecommendButton from "@/components/RecommendButton.vue";
import RandomAnimation from "@/components/RandomAnimation.vue";
import AnalysisAnimation from "@/components/AnalysisAnimation.vue";
import AIAnimation from "@/components/AIAnimation.vue";
import placeholderImage from "@/assets/eat_bear_logo.png";

// 스토어
const store = useRecommendStore();

// 라우터/props
const props = defineProps({ initialMode: { type: String, default: null } });
const route = useRoute();
const router = useRouter();
const mode = ref(props.initialMode ?? route.params.mode ?? "random");

// 모드 배열
const modes = [
  { id: "random", label: "랜덤 추천", icon: "🎲", cost: 1, animation: RandomAnimation },
  { id: "history", label: "최근 3일 기반", icon: "🕓", cost: 1, animation: AnalysisAnimation },
  { id: "ai", label: "AI 추천", icon: "🤖", cost: 2, animation: AIAnimation },
];

// 컴포넌트용 computed
const currentMode = computed(() => modes.find((m) => m.id === mode.value));
const spoonCost = computed(() => currentMode.value.cost);
const spoonCount = computed(() => store.spoonCount);
const loading = computed(() => store.loading);
const recommendation = computed(() => store.recommendation);
const hasRun = computed(() => store.hasRun);
const runCount = computed(() => store.runCount);
const showNoHistoryPrompt = computed(() => store.showNoHistoryPrompt);
const showLoginPrompt = computed(() => store.showLoginPrompt);
const buttonLabel = computed(() => currentMode.value.label);
const modeLabel = computed(() => currentMode.value.label);
const resultTitle = computed(() => `${modeLabel.value} 메뉴`);
const animationComponent = computed(() => currentMode.value.animation);
const expanded = computed(() => store.expanded);

// 함수 바인딩
const { fetchRecommendation, fetchRemainingSpoons, fetchHistoryRecords, resetRecommend, requireLogin, showNoHistory, hideNoHistory, showLogin, hideLogin } = store;

// 실행 버튼 클릭
function onRun() {
  if (!requireLogin()) return;
  if (store.remainingSpoons <= 0) {
    showNoHistory();
    return;
  }
  if ((mode.value === "history" || mode.value === "ai") && store.historyRecords.length === 0) {
    showNoHistory();
    return;
  }
  store.remainingSpoons -= spoonCost.value;
  store.hasRun = true;
  store.loading = true;
  store.runCount++;
}

// 로그인 이동
function goToLogin() {
  router.push("/login");
}

// 마운트 시 데이터 로드
onMounted(async () => {
  await fetchRemainingSpoons();
  await fetchHistoryRecords();
});

// 라우트 모드 변경 감지
watch(
  () => route.params.mode,
  (m) => {
    if (!props.initialMode && m) {
      mode.value = m;
      resetRecommend();
      fetchRemainingSpoons();
    }
  }
);
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
