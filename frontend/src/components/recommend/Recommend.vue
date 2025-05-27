<template>
  <div class="recommend-bg">
    <!-- ⭐️ 3일치 식단 기록 안내 모달 ⭐️ -->
    <div v-if="showPromptModal" class="modal-backdrop">
      <div class="modal-dialog">
        <h3>식단 기록이 필요해요!</h3>
        <p>
          최근 3일간 식단 기록이 없어요.
          <br />
          먼저 식단(다이어리)을 작성해 주세요.
        </p>
        <button class="go-diary-btn" @click="goDiaryPage">작성하러 가기</button>
      </div>
    </div>

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
                <RecommendButton :label="buttonLabel" :cost="spoonCost" :spoonCount="spoonCount" :loading="loading" :disabled="loading || spoonCount < spoonCost" @run="onRun" />
              </div>

              <div v-if="!isResultReady && !showNoHistoryPrompt && !showLoginPrompt" class="content-area">
                <section class="main-view" :class="{ expanded, 'chat-bg': isChatting }">
                  <div v-if="!hasRun" class="placeholder">
                    <img :src="placeholderImage" alt="추천 준비 중" />
                  </div>
                  <div v-else-if="!isResultReady" class="animation-wrapper">
                    <component :is="animationComponent" :key="mode + '-' + runCount" @done="onAnimationDone" @ai-finish="onAiChatDone" />
                  </div>
                </section>
              </div>

              <!-- 히스토리 없음 안내 -->
              <div v-if="showNoHistoryPrompt" class="prompt-message">
                <h3>🍽️ 식단 기록이 부족해요</h3>
                <p>{{ mode === 'history' ? '새로운 맛' : '취향 맞춤' }} 추천을 위해서는<br>최근 3일간의 식단 기록이 필요합니다.</p>
                <button @click="goToRegister" class="prompt-button">식단 등록하러 가기</button>
              </div>

              <!-- 로그인 안내 -->
              <div v-if="showLoginPrompt" class="prompt-message">
                <h3>🔐 로그인이 필요해요</h3>
                <p>개인화된 추천을 받으시려면<br>로그인해주세요.</p>
                <button @click="goToLogin" class="prompt-button">로그인하러 가기</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 오른쪽: 추천 결과 카드 + 차트(나란히) -->
      <transition name="fade">
        <div v-if="isResultReady" class="result-outer result-row-flex">
          <aside class="result-box" :class="{ expanded }">
            <h3>{{ resultTitle }}</h3>
            <div class="food-card">
              <img :src="foodImage" alt="추천 음식" />
              <div class="food-main-info">
                <p class="food-name">{{ safeRecommendation.foodName }}</p>
                <p class="food-desc">
                  {{ safeRecommendation.majorCategory }} /
                  {{ safeRecommendation.subCategory }}
                </p>
              </div>
              <div class="food-nutrition">
                <span>
                  <span class="nutri-label">칼로리</span>
                  🔥
                  <b>{{ safeRecommendation.kcal }}</b>
                  kcal
                </span>
                <span>
                  <span class="nutri-label">탄수화물</span>
                  🍚
                  <b>{{ safeRecommendation.carbohydrate }}</b>
                  g
                </span>
                <span>
                  <span class="nutri-label">단백질</span>
                  🥩
                  <b>{{ safeRecommendation.protein }}</b>
                  g
                </span>
                <span>
                  <span class="nutri-label">지방</span>
                  🥑
                  <b>{{ safeRecommendation.fat }}</b>
                  g
                </span>
              </div>
              <div class="food-etc">
                <span>💧 {{ safeRecommendation.moisture }}g</span>
                <span>🍬 {{ safeRecommendation.sugar }}g</span>
                <span>🧂 {{ safeRecommendation.sodium }}mg</span>
                <span>🥚 {{ safeRecommendation.cholesterol }}mg</span>
                <span>🥄 {{ safeRecommendation.servingSize }}g</span>
              </div>
              <a class="coupang-link-btn" :href="coupangUrl" target="_blank" rel="noopener">🛒 쿠팡에서 "{{ searchKeyword }}" 검색하기</a>
            </div>
          </aside>
          <!-- ⭐ 카드 옆에 차트! ⭐ -->
          <NutritionCompareChart :food="safeRecommendation" />
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
import recommendSuccessLogo from "@/assets/recommend_sucess_logo.png";
import NutritionCompareChart from "@/components/NutritionCompareChart.vue";

// ⭐️ 3일치 식단 기록 안내 모달 제어
const showPromptModal = ref(false);
const store = useRecommendStore();
const router = useRouter();

function goDiaryPage() {
  showPromptModal.value = false;
  router.replace({ name: "Calendar" }); // 실제 캘린더/다이어리 페이지 name!
}

onMounted(async () => {
  await store.fetchRemainingSpoons();
  await store.fetchHistoryRecords();
  if (store.showNoHistoryPrompt) showPromptModal.value = true;
});
watch(
  () => store.showNoHistoryPrompt,
  (v) => {
    if (v) showPromptModal.value = true;
  }
);

// ===== 기존 코드 =====
const props = defineProps({ initialMode: { type: String, default: null } });
const route = useRoute();
const mode = ref(props.initialMode ?? route.params.mode ?? "random");
const isChatting = ref(false);
const isResultReady = ref(false);

const currentMode = computed(() => modes.find((m) => m.id === mode.value));
const spoonCost = computed(() => currentMode.value?.cost || 1);
const spoonCount = computed(() => store.remainingSpoons);
const loading = computed(() => store.loading);
const recommendation = computed(() => store.recommendation);
const hasRun = computed(() => store.hasRun);
const runCount = computed(() => store.runCount);
const showNoHistoryPrompt = computed(() => store.showNoHistoryPrompt);
const showLoginPrompt = computed(() => store.showLoginPrompt);
const buttonLabel = computed(() => currentMode.value?.label || '추천하기');
const resultTitle = computed(() => `${currentMode.value?.label || '추천'} 메뉴`);
const animationComponent = computed(() => currentMode.value?.animation || RandomAnimation);
const expanded = computed(() => store.expanded);
const foodImage = computed(() => recommendSuccessLogo);

// 안전한 추천 데이터 (누락된 부분 추가)
const safeRecommendation = computed(() => {
  const defaultFood = {
    foodName: '추천 음식',
    majorCategory: '기타',
    subCategory: '기타',
    kcal: 0,
    carbohydrate: 0,
    protein: 0,
    fat: 0,
    moisture: 0,
    sugar: 0,
    sodium: 0,
    cholesterol: 0,
    servingSize: 100
  };
  
  return recommendation.value ? { ...defaultFood, ...recommendation.value } : defaultFood;
});

const searchKeyword = computed(() => safeRecommendation.value.foodName || "요거트 샐러드");
const coupangUrl = computed(() => `https://www.coupang.com/np/search?q=${encodeURIComponent(searchKeyword.value)}`);

// 추천 모드 정보
const modes = [
  { id: "random", label: "랜덤 추천", icon: "🎲", desc: "랜덤으로 추천", cost: 1, animation: RandomAnimation },
  { id: "history", label: "새로운 맛", icon: "🕓", desc: "3일 내 식단과 가장 거리가 먼 음식 추천", cost: 1, animation: AnalysisAnimation },
  { id: "ai", label: "AI 추천", icon: "🤖", desc: "내 식단과 건강정보를 활용한 RAG AI추천", cost: 2, animation: AIAnimation },
  { id: "custom", label: "취향 맞춤", icon: "✨", desc: "최근 음식과 비슷한 추천", cost: 1, animation: CustomAnimation },
];

// 실행 버튼 핸들러 (수정)
async function onRun() {
  if (spoonCount.value < spoonCost.value) {
    alert('스푼이 부족합니다!');
    return;
  }

  // 스푼 사용
  await store.useSpoons(spoonCost.value);
  
  store.hasRun = true;
  store.runCount++;
  isResultReady.value = false;
  isChatting.value = true;

  if (mode.value !== "ai") {
    // 일반 추천 (random, history, custom)
    store.fetchRecommendation(mode.value).catch(() => {
      // 에러는 store에서 처리됨
      isChatting.value = false;
    });
  }
  // AI 모드는 애니메이션에서 사용자 입력 받은 후 onAiChatDone에서 처리
}

function onAnimationDone() {
  isResultReady.value = true;
  isChatting.value = false;
}

function onAiChatDone(payload) {
  store
    .fetchAiRecommendation(payload)
    .then(() => {
      isResultReady.value = true;
      isChatting.value = false;
    })
    .catch((err) => {
      console.error("AI 추천 오류:", err);
      isChatting.value = false;
    });
}

function goMode(id) {
  if (loading.value) return;
  if (id === mode.value) return;
  router.push(`/recommend/${id}`);
  mode.value = id;
  store.resetRecommend();
  store.fetchRemainingSpoons();
  store.fetchHistoryRecords();
  isResultReady.value = false;
  isChatting.value = false;
}

onMounted(async () => {
  await store.fetchRemainingSpoons();
  await store.fetchHistoryRecords();
  store.hasRun = false;
  isResultReady.value = false;
  isChatting.value = false;
});

watch(
  () => route.params.mode,
  (m) => {
    if (!props.initialMode && m) {
      mode.value = m;
      store.resetRecommend();
      store.fetchRemainingSpoons();
      store.fetchHistoryRecords();
      isResultReady.value = false;
      isChatting.value = false;
    }
  }
);
</script>

<style scoped>
/* ===== ⭐️ 안내 모달 스타일 ===== */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(40, 60, 60, 0.38);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
}
.modal-dialog {
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 6px 40px #24c7c840;
  padding: 36px 38px 28px 38px;
  text-align: center;
  max-width: 96vw;
}
.modal-dialog h3 {
  color: #17cfa6;
  font-size: 1.28rem;
  font-weight: 700;
  margin-bottom: 10px;
}
.modal-dialog p {
  color: #555;
  font-size: 1.06rem;
  margin-bottom: 18px;
  line-height: 1.5;
}
.go-diary-btn {
  background: linear-gradient(90deg, #21d59b 0%, #ffc83d 120%);
  color: #fff;
  font-size: 1.12rem;
  font-weight: 700;
  padding: 12px 36px;
  border: none;
  border-radius: 14px;
  box-shadow: 0 3px 16px #21d59b33;
  cursor: pointer;
  transition: background 0.18s, transform 0.14s;
}
.go-diary-btn:hover {
  background: linear-gradient(90deg, #19b37b 0%, #e4af2d 120%);
  color: #fffbe0;
  transform: translateY(-2px) scale(1.03);
}

/* ===== 배경 및 전체 레이아웃 ===== */
/* 기존 스타일 + 추가 프롬프트 스타일 */

.prompt-message {
  text-align: center;
  padding: 3rem 2rem;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  margin: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 2px solid #f0f8ff;
}

.prompt-message h3 {
  margin-bottom: 1rem;
  color: #333;
  font-size: 1.5rem;
  font-weight: 700;
}

.prompt-message p {
  color: #666;
  line-height: 1.6;
  margin-bottom: 2rem;
  font-size: 1rem;
}

.prompt-button {
  background: linear-gradient(90deg, #21d59b 0%, #ffc83d 100%);
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.2s ease;
}

.prompt-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(33, 213, 155, 0.3);
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.5s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* 기존 스타일들 그대로 유지 */
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

/* 나머지 스타일들은 기존과 동일... */
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

/* 목업 및 나머지 스타일들... (기존과 동일) */
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

.run-button {
  margin: 8px 0 16px 0;
}

.main-view {
  background: transparent;
  transition: background 0.45s cubic-bezier(0.33, 1, 0.68, 1);
  border-radius: 16px;
  min-height: 750px;
}

.main-view.chat-bg {
  background: #152044;
}

.placeholder img {
  width: 55%;
  max-width: 200px;
  min-width: 80px;
  height: auto;
  margin: 12% auto 18px auto;
  display: block;
}

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
/* 쿠팡 파트너스 버튼 스타일 */
.coupang-link-btn {
  display: inline-block;
  margin: 22px auto 0 auto;
  background: linear-gradient(90deg, #ffe972 0%, #ffb18c 100%);
  color: #1a1a1a;
  font-weight: 700;
  border-radius: 14px;
  padding: 12px 26px;
  font-size: 1.08rem;
  text-decoration: none;
  box-shadow: 0 2px 14px #ffbf2f33;
  transition: background 0.18s, transform 0.14s;
  cursor: pointer;
  border: none;
  outline: none;
  letter-spacing: 0.03em;
  position: relative;
}

.coupang-link-btn:hover {
  background: linear-gradient(90deg, #ffdb4a 0%, #ff8640 100%);
  color: #fff;
  transform: translateY(-2px) scale(1.04);
  box-shadow: 0 6px 28px #ffbf2f44;
}

/* ===== 반응형 ===== */
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
  .result-outer.result-row-flex {
    display: flex;
    flex-direction: row;
    align-items: flex-start;
    gap: 34px;
  }
  .nutrition-chart-horizontal {
    margin: 38px 0 0 0;
    padding: 0 8px 10px 8px;
    width: 900px;
    min-width: 900px;
    max-width: 100%;
    height: 700px;
    display: flex;
    justify-content: center;
    align-items: flex-start;
  }
  /* 세로 쌓기 */
  .result-outer.result-row-flex {
    flex-direction: column;
    align-items: center;
    gap: 16px;
  }
}
</style>
