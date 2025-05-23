<template>
  <div class="recommend-bg">
    <div class="main-layout">
      <!-- 좌측: 추천 모드 2x2 버튼 -->
      <div class="mode-sidebar">
        <div class="mode-grid">
          <button v-for="m in modes" :key="m.id" class="mode-btn" :class="{ active: mode === m.id }" @click="goMode(m.id)" type="button" :disabled="loading">
            <div class="icon">{{ m.icon }}</div>
            <div class="title">{{ m.label }}</div>
            <div class="desc">{{ m.desc }}</div>
          </button>
        </div>
      </div>
      <!-- 가운데: 스마트폰 목업(추천) -->
      <div class="mockup-wrap">
        <div class="mockup-phone">
          <div class="mockup-notch"></div>
          <div class="mockup-content">
            <div class="recommend-inner">
              <div class="run-button">
                <RecommendButton :label="buttonLabel" :cost="spoonCost" :spoonCount="spoonCount" :loading="loading" :disabled="loading" @run="onRun" />
              </div>
              <div v-show="!showNoHistoryPrompt && !showLoginPrompt" class="content-area">
                <section class="main-view" :class="{ expanded, 'chat-bg': isChatting }">
                  <div v-if="!hasRun" class="placeholder">
                    <img :src="placeholderImage" alt="추천 준비 중" />
                  </div>
                  <div v-if="hasRun && !isResultReady" class="animation-wrapper">
                    <!-- ⭐ AI모드는 ai-finish 이벤트도 받음 -->
                    <component :is="animationComponent" :key="mode + '-' + runCount" @done="onAnimationDone" @ai-finish="onAiChatDone" />
                  </div>
                </section>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 오른쪽: 추천 결과 카드 -->
      <transition name="fade">
        <div v-if="isResultReady && recommendation" class="result-outer">
          <aside class="result-box" :class="{ expanded }">
            <h3>{{ resultTitle }}</h3>
            <div class="food-card">
              <img :src="foodImage" alt="추천 음식" />
              <div class="food-main-info">
                <p class="food-name">{{ recommendation.foodName }}</p>
                <p class="food-desc">{{ recommendation.majorCategory }} / {{ recommendation.subCategory }}</p>
              </div>
              <div class="food-nutrition">
                <span>
                  <span class="nutri-label">칼로리</span>
                  🔥
                  <b>{{ recommendation.kcal }}</b>
                  kcal
                </span>
                <span>
                  <span class="nutri-label">탄수화물</span>
                  🍚
                  <b>{{ recommendation.carbohydrate }}</b>
                  g
                </span>
                <span>
                  <span class="nutri-label">단백질</span>
                  🥩
                  <b>{{ recommendation.protein }}</b>
                  g
                </span>
                <span>
                  <span class="nutri-label">지방</span>
                  🥑
                  <b>{{ recommendation.fat }}</b>
                  g
                </span>
              </div>
              <div class="food-etc">
                <span>
                  <span class="nutri-label">수분</span>
                  💧 {{ recommendation.moisture }}g
                </span>
                <span>
                  <span class="nutri-label">당류</span>
                  🍬 {{ recommendation.sugar }}g
                </span>
                <span>
                  <span class="nutri-label">나트륨</span>
                  🧂 {{ recommendation.sodium }}mg
                </span>
                <span>
                  <span class="nutri-label">콜레스테롤</span>
                  🥚 {{ recommendation.cholesterol }}mg
                </span>
                <span>
                  <span class="nutri-label">1회 제공량</span>
                  🥄 {{ recommendation.servingSize }}g
                </span>
              </div>
            </div>
          </aside>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, defineProps } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useRecommendStore } from "@/stores/recommend";
import RecommendButton from "@/components/RecommendButton.vue";
import RandomAnimation from "@/components/animation/RandomAnimation.vue";
import AnalysisAnimation from "@/components/animation/AnalysisAnimation.vue";
import CustomAnimation from "@/components/animation/CustomAnimation.vue";
import AIAnimation from "@/components/animation/AIAnimation.vue";
import placeholderImage from "@/assets/loading_logo.png";
import recommend_sucess_logo from "@/assets/recommend_sucess_logo.png";

// 추천 모드 정보
const modes = [
  { id: "random", label: "랜덤 추천", icon: "🎲", desc: "랜덤으로 추천", cost: 1, animation: RandomAnimation },
  { id: "history", label: "새로운 맛", icon: "🕓", desc: "3일 내 식단과 가장 거리가 먼 음식 추천", cost: 1, animation: AnalysisAnimation },
  { id: "ai", label: "AI 추천", icon: "🤖", desc: "내 식단 기록 내역과 건강정보를 활용한 RAG 기반 AI추천", cost: 2, animation: AIAnimation },
  { id: "custom", label: "취향 맞춤", icon: "✨", desc: "내가 최근 먹은 음식들과 비슷한 음식 추천", cost: 1, animation: CustomAnimation },
];

const store = useRecommendStore();
const props = defineProps({ initialMode: { type: String, default: null } });
const route = useRoute();
const router = useRouter();
const mode = ref(props.initialMode ?? route.params.mode ?? "random");

const isChatting = ref(false);
const isResultReady = ref(false);

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
const foodImage = computed(() => recommend_sucess_logo);

const { fetchRecommendation, fetchAiRecommendation, fetchRemainingSpoons, fetchHistoryRecords, resetRecommend } = store;

// 실행 버튼
function onRun() {
  store.remainingSpoons -= spoonCost.value;
  store.hasRun = true;
  store.loading = true;
  store.runCount++;
  isResultReady.value = false;
  isChatting.value = true;

  // AI모드는 채팅 종료 후 API 호출
  if (mode.value !== "ai") {
    store.fetchRecommendation(mode.value).then(() => {
      // 애니메이션 컴포넌트에서 done 이벤트로 결과 띄움
    });
  }
}

// 채팅 애니메이션이 끝나면(랜덤/히스토리/커스텀)
function onAnimationDone() {
  isResultReady.value = true;
  store.loading = false;
  isChatting.value = false;
}

// AI 채팅 입력 다 끝나면
function onAiChatDone(payload) {
  // payload: { prefer: "...", avoid: "..." } 형태라고 가정
  store.fetchAiRecommendation(payload).then(() => {
    isResultReady.value = true;
    store.loading = false;
    isChatting.value = false;
  });
}

// 모드 바꿀 때
function goMode(id) {
  if (loading.value) return; // 로딩 중이면 무시
  if (id !== mode.value) {
    router.push(`/recommend/${id}`);
    mode.value = id;
    resetRecommend();
    fetchRemainingSpoons();
    isResultReady.value = false;
    isChatting.value = false;
  }
}

// mount
onMounted(async () => {
  await fetchRemainingSpoons();
  await fetchHistoryRecords();
  store.hasRun = false;
  isResultReady.value = false;
  isChatting.value = false;
});
watch(
  () => route.params.mode,
  (m) => {
    if (!props.initialMode && m) {
      mode.value = m;
      resetRecommend();
      fetchRemainingSpoons();
      isResultReady.value = false;
      isChatting.value = false;
    }
  }
);
</script>

<style scoped>
/* 배경 및 전체 레이아웃 */
.recommend-bg {
  width: 100vw;
  min-height: 100vh;
  background: linear-gradient(135deg, #d9f6ee 0%, #fff 100%);
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  padding: 4vw 0 3vw 0;
  box-sizing: border-box;
}
.main-layout {
  display: flex;
  width: 100%;
  max-width: 1400px;
  min-height: 800px;
  justify-content: flex-start;
  gap: 56px;
}
/* --- 좌: 모드 버튼 --- */
.mode-sidebar {
  flex: 0 0 370px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 42px;
  margin-left: 10%;
}
.mode-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 22px 18px;
  width: 340px;
}
.mode-btn {
  background: #f8fffc;
  border-radius: 18px;
  box-shadow: 0 2px 14px #20c59c11;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28px 8px 22px 8px;
  border: none;
  transition: box-shadow 0.12s, transform 0.14s;
  text-decoration: none;
  color: #189c88;
  cursor: pointer;
  outline: none;
  font-family: inherit;
  min-width: 0;
  min-height: 120px;
  font-size: 1.05rem;
}
.mode-btn.active {
  background: linear-gradient(90deg, #21d59b 0%, #ffc83d 120%);
  color: #fff !important;
  box-shadow: 0 5px 20px #21d59b33;
  transform: scale(1.04);
}
.mode-btn:hover:not(.active) {
  box-shadow: 0 6px 26px #21d59b22;
  transform: translateY(-3px) scale(1.03);
}
/* 버튼 비활성화(로딩중) */
.mode-btn:disabled,
.mode-btn.disabled {
  pointer-events: none;
  opacity: 0.55;
  filter: grayscale(0.1);
  cursor: not-allowed;
}
.icon {
  font-size: 2.4rem;
  margin-bottom: 6px;
  filter: drop-shadow(0 2px 10px #21d59b14);
}
.title {
  font-size: 1.22rem;
  font-weight: bold;
  margin: 6px 0 2px 0;
  letter-spacing: 0.4px;
}
.desc {
  font-size: 1.04rem;
  color: #93bbb2;
  margin-top: 2px;
  text-align: center;
  line-height: 1.32;
  font-weight: 600;
}
.mode-btn.active .desc {
  color: #fffbe0;
}

/* --- 중앙: 목업 --- */
.mockup-wrap {
  flex: 0 0 530px;
  display: flex;
  justify-content: flex-start;
  align-items: center;
}
.mockup-phone {
  width: 480px;
  max-width: 98vw;
  height: 920px;
  max-height: 98vh;
  border-radius: 38px;
  background: #fff;
  box-shadow: 0 14px 40px 0 #21d59b30, 0 3px 18px 0 #20c59c15;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border: 2px solid #e3e9ee;
  transition: box-shadow 0.2s;
}
.mockup-notch {
  width: 80px;
  height: 16px;
  background: #eee;
  border-radius: 10px;
  position: absolute;
  top: 18px;
  left: 50%;
  transform: translateX(-50%);
  opacity: 0.55;
  z-index: 2;
}
.mockup-content {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  overflow: auto;
  padding: 46px 0 0 0;
  position: relative;
  height: 100%;
}
.recommend-inner {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 0 22px 24px 22px;
  box-sizing: border-box;
}

/* 실행 버튼/추천 콘텐츠~ */
.run-button {
  margin: 8px 0 16px 0;
}

/* 채팅중일 때만 폰 배경 어둡게 전환 */
.main-view {
  background: transparent;
  transition: background 0.45s cubic-bezier(0.33, 1, 0.68, 1);
  border-radius: 16px;
  min-height: 750px;
}
.main-view.chat-bg {
  background: #152044;
  /* 원하는 색상으로! (밤하늘 느낌) */
}
.placeholder img {
  width: 55%;
  max-width: 200px;
  min-width: 80px;
  height: auto;
  margin: 12% auto 18px auto;
  display: block;
}
/* 결과 카드: 목업 오른쪽! */
.result-outer {
  flex: 0 0 370px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  min-width: 300px;
}
.result-box {
  background: #fff;
  border-radius: 22px;
  box-shadow: 0 6px 32px #21d59b1b;
  padding: 38px 26px 22px 26px;
  min-width: 260px;
  max-width: 360px;
  margin: 0;
  text-align: center;
  animation: pop-card 0.45s cubic-bezier(0.33, 1, 0.68, 1);
}
@keyframes pop-card {
  0% {
    opacity: 0;
    transform: scale(0.85) translateY(40px);
  }
  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* 추천 카드 정보 디자인 */
.food-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  text-align: center;
  padding-top: 6px;
}
.food-card img {
  max-width: 120px;
  border-radius: 16px;
  box-shadow: 0 2px 8px #33d9b122;
  margin-bottom: 4px;
}
.food-main-info {
  margin-bottom: 8px;
}
.food-name {
  font-size: 1.2rem;
  font-weight: 700;
  margin-bottom: 2px;
}
.food-desc {
  font-size: 1.05rem;
  color: #75bca5;
}
.food-nutrition,
.food-etc {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
  justify-content: center;
  margin-top: 6px;
  font-size: 0.97rem;
  color: #555;
}
.food-nutrition span b {
  color: #17cfa6;
}

/* 반응형 */
@media (max-width: 1100px) {
  .main-layout {
    flex-direction: column;
    align-items: center;
    gap: 26px;
  }
  .result-outer {
    flex: 0 0 auto;
    min-width: 0;
    margin-top: 24px;
    justify-content: center;
  }
  .result-box {
    max-width: 98vw;
  }
  .mockup-wrap {
    justify-content: center;
  }
}
@media (max-width: 700px) {
  .main-layout {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  .mode-grid {
    grid-template-columns: 1fr;
    gap: 12px 0;
    width: 96vw;
    max-width: 99vw;
  }
  .mockup-phone {
    width: 99vw;
    min-width: 0;
    height: 88vh;
    min-height: 420px;
    border-radius: 18px;
    max-height: 98vh;
  }
  .result-outer {
    justify-content: center;
    margin-top: 10px;
  }
  .result-box {
    padding: 22px 8vw 22px 8vw;
    min-width: 0;
  }
  .placeholder img {
    width: 38vw;
    max-width: 100px;
    min-width: 52px;
    margin: 24vw auto 16px auto;
  }
}
</style>
