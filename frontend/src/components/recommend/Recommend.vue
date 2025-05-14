<template>
  <div class="recommend-page">
    <!-- 실행 버튼 -->
    <div class="run-button">
      <RecommendButton :label="buttonLabel" :cost="spoonCost" :spoonCount="spoonCount" :loading="loading" @run="onRun" />
    </div>

    <!-- 모드 선택 버튼 -->
    <div class="page-buttons">
      <router-link v-for="m in modes" :key="m.id" :to="`/recommend/${m.id}`" class="page-btn" :class="[m.id, { active: mode === m.id }]">
        <span class="btn-pill">{{ m.label }}</span>
        <span class="btn-icon">{{ m.icon }}</span>
      </router-link>
    </div>

    <!-- 전체 콘텐츠영역 -->
    <div v-show="!showNoHistoryPrompt" class="content-area">
      <!-- 중앙 영역 -->
      <section class="main-view" :class="{ expanded }">
        <!-- 플레이스홀더 -->
        <div v-if="!hasRun" class="placeholder">
          <img :src="placeholderImage" alt="추천 준비 중" />
          <p>버튼을 눌러 {{ modeLabel }} 받기</p>
        </div>
        <!-- 애니메이션 -->
        <div v-if="hasRun" class="animation-wrapper">
          <component :is="animationComponent" :key="mode + '-' + runCount" @done="fetchResult" />
        </div>
      </section>

      <!-- 결과 영역 -->
      <aside v-if="result" class="result-box" :class="{ expanded }">
        <h3>{{ resultTitle }}</h3>
        <div class="food-card">
          <img :src="result.image" alt="추천 음식" />
          <p class="food-name">{{ result.name }}</p>
          <p class="food-detail">{{ result.detail }}</p>
        </div>
      </aside>
    </div>

    <!-- 모달 팝업 -->
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

<script>
import { ref, computed, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import RecommendButton from "@/components/RecommendButton.vue";
import RandomAnimation from "@/components/RandomAnimation.vue";
import AnalysisAnimation from "@/components/AnalysisAnimation.vue";
import AIAnimation from "@/components/AIAnimation.vue";
import placeholderImage from "@/assets/eat_bear_logo.png";

export default {
  name: "Recommend",
  components: {
    RecommendButton,
    RandomAnimation,
    AnalysisAnimation,
    AIAnimation,
  },
  props: {
    initialMode: { type: String, default: null },
  },
  setup(props) {
    const route = useRoute();
    const modes = [
      { id: "random", label: "랜덤 추천", icon: "🎲", cost: 1, animation: RandomAnimation },
      { id: "history", label: "최근 3일 기반", icon: "🕓", cost: 1, animation: AnalysisAnimation },
      { id: "ai", label: "AI 추천", icon: "🤖", cost: 2, animation: AIAnimation },
    ];
    const mode = ref(props.initialMode ?? route.params.mode ?? "random");
    const spoonCount = ref(5);
    const loading = ref(false);
    const result = ref(null);
    const hasRun = ref(false);
    const runCount = ref(0);
    const showNoHistoryPrompt = ref(false);
    const historyRecords = ref([]);

    onMounted(async () => {
      try {
        historyRecords.value = await fetch("/api/history").then((r) => r.json());
      } catch {
        historyRecords.value = [
          { date: "2025-05-12", name: "김치찌개" },
          { date: "2025-05-13", name: "된장찌개" },
          { date: "2025-05-14", name: "비빔밥" },
        ];
      }
    });

    watch(
      () => route.params.mode,
      (m) => {
        if (m && !props.initialMode) {
          mode.value = m;
          result.value = null;
          hasRun.value = false;
          runCount.value = 0;
        }
      }
    );

    const spoonCost = computed(() => modes.find((m) => m.id === mode.value).cost);
    const buttonLabel = computed(() => modes.find((m) => m.id === mode.value).label);
    const modeLabel = computed(() => modes.find((m) => m.id === mode.value).label);
    const resultTitle = computed(() => `${modeLabel.value} 메뉴`);
    const animationComponent = computed(() => modes.find((m) => m.id === mode.value).animation);
    const expanded = computed(() => loading.value || !!result.value);

    function onRun() {
      if ((mode.value === "history" || mode.value === "ai") && historyRecords.value.length === 0) {
        showNoHistoryPrompt.value = true;
        return;
      }
      spoonCount.value -= spoonCost.value;
      hasRun.value = true;
      loading.value = true;
      runCount.value++;
    }

    async function fetchResult() {
      try {
        const res = await fetch(`/api/recommend?mode=${mode.value}`);
        if (!res.ok) throw new Error("Network error");
        result.value = await res.json();
      } catch (e) {
        console.error(e);
        result.value = {
          name: "비빔밥",
          detail: "야채와 고기가 어우러진 맛",
          image: placeholderImage,
        };
      } finally {
        loading.value = false;
      }
    }

    return {
      modes,
      mode,
      spoonCount,
      loading,
      result,
      hasRun,
      runCount,
      showNoHistoryPrompt,
      placeholderImage,
      spoonCost,
      buttonLabel,
      modeLabel,
      resultTitle,
      animationComponent,
      onRun,
      fetchResult,
      expanded,
    };
  },
};
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
