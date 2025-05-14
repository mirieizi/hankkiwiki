<template>
  <!-- No history popup -->
  <transition name="fade">
    <div v-if="showNoHistoryPrompt" class="modal-overlay" @click.self="showNoHistoryPrompt = false">
      <div class="modal">
        <p>최근 3일간 식사 기록이 없습니다.</p>
        <div class="modal-buttons">
          <router-link to="/calendar" class="modal-btn">식사 입력하러 가기</router-link>
          <router-link to="/recommend/random" class="modal-btn">랜덤으로 추출하기</router-link>
        </div>
        <button class="modal-close" @click="showNoHistoryPrompt = false">✕</button>
      </div>
    </div>
  </transition>

  <div class="recommend-page">
    <!-- 0) 추천 실행 버튼 -->
    <div class="run-button">
      <RecommendButton :label="buttonLabel" :cost="spoonCost" :spoonCount="spoonCount" :loading="loading" @run="onRun" />
    </div>

    <!-- 1) 왼쪽 버튼 열 -->
    <div class="page-buttons">
      <router-link v-for="m in modes" :key="m.id" :to="`/recommend/${m.id}`" class="page-btn" :class="[m.id, { active: mode === m.id }]">
        <div class="btn-content">
          <span class="btn-pill">{{ m.label }}</span>
          <span class="btn-icon">{{ m.icon }}</span>
        </div>
      </router-link>
    </div>

    <!-- 2) 가운데 애니메이션 영역 고정 -->
    <section class="main-view">
      <!-- loading 상태에서만 AI 애니메이션 표시 -->
      <AIAnimation v-if="loading" @done="fetchResult" />
      <!-- 실행 전/완료 후 빈 화면 대신 플레이스홀더 삽입 -->
      <div v-else class="placeholder">
        <img :src="placeholderImage" alt="추천 준비 중" />
        <p>버튼을 눌러 추천을 시작해보세요!</p>
      </div>
    </section>

    <!-- 3) 오른쪽 추천 결과 -->
    <aside class="result-box" v-if="result">
      <h3>오늘의 추천 메뉴</h3>
      <div class="food-card">
        <img :src="result.image" alt="추천 음식" />
        <p class="food-name">{{ result.name }}</p>
        <p class="food-detail">{{ result.detail }}</p>
      </div>
    </aside>
  </div>
</template>

<script>
import { useRoute } from "vue-router";
import AIAnimation from "@/components/AIAnimation.vue";
import RecommendButton from "@/components/RecommendButton.vue";
import { ref, computed } from "vue";
import placeholderImage from "@/assets/eat_bear_logo.png";

export default {
  name: "AIRecommend",
  components: { AIAnimation, RecommendButton },
  setup() {
    const route = useRoute();
    const spoonCount = ref(5);
    const loading = ref(false);
    const result = ref(null);
    const historyRecords = ref([]);
    const showNoHistoryPrompt = ref(false);
    const historyEmpty = computed(() => historyRecords.value.length === 0);

    const modes = [
      { id: "random", label: "랜덤 추천", icon: "🎲" },
      { id: "history", label: "최근에 먹은 거 빼고", icon: "🕓" },
      { id: "ai", label: "AI 맛추 추천", icon: "🤖" },
    ];

    const mode = computed(() => route.params.mode || "history");
    const spoonCost = computed(() => 1); // AI 추천은 숟가락 1개
    const buttonLabel = computed(() => "최근에 먹은 거 빼고!");

    function onRun() {
      // 기록이 없으면 팝업 표시 후 중단
      if (historyEmpty.value) {
        showNoHistoryPrompt.value = true;
        return;
      }
      if (spoonCount.value < spoonCost.value) return;
      spoonCount.value -= spoonCost.value;
      loading.value = true;
    }

    function fetchResult() {
      const dummy = {
        ai: { name: "비빔밥", detail: "다채로운 야채와 고기", image: "/images/bibimbap.jpg" },
      };
      result.value = dummy.ai;
      loading.value = false;
    }

    return {
      modes,
      mode,
      spoonCount,
      spoonCost,
      buttonLabel,
      loading,
      result,
      onRun,
      fetchResult,
      placeholderImage,
      historyRecords,
      historyEmpty,
      showNoHistoryPrompt,
    };
  },
};
</script>

<style scoped>
.recommend-page {
  display: grid;
  grid-template-columns: 0.5fr 1fr 1.5fr;
  grid-template-rows: auto auto 1fr;
  gap: 16px;
  height: 100%;
  padding: 16px;
  box-sizing: border-box;
}
.run-button {
  grid-column: 1 / span 3;
  margin-bottom: 16px;
}
.page-buttons {
  grid-column: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.page-btn {
  width: 100%;
  text-decoration: none;
  border-radius: 36px;
  overflow: hidden;
  transition: box-shadow 0.2s;
}
.page-btn .btn-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px;
  padding: 0 24px;
}
.page-pill {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 24px;
  padding: 8px 16px;
  font-size: 16px;
  color: #333;
}
.btn-icon {
  font-size: 24px;
}
.page-btn.random {
  background: #8ecdf5;
}
.page-btn.history {
  background: #ffe66d;
}
.page-btn.ai {
  background: #f8bbd0;
}
.page-btn.active {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
.main-view {
  grid-column: 2;
  background: #fafafa;
  border: 2px solid #000;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.placeholder {
  text-align: center;
}
.placeholder img {
  max-width: 60%;
  height: auto;
  display: block;
  margin: 0 auto 8px;
}
.result-box {
  grid-column: 3;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.food-card {
  text-align: center;
}
.food-card img {
  width: 100%;
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
@media (max-width: 768px) {
  .recommend-page {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto auto auto;
  }
  .page-buttons {
    flex-direction: row;
    overflow-x: auto;
  }
  .result-box {
    margin-top: 16px;
  }
}
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
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
  gap: 16px;
  margin-top: 16px;
}
.modal-btn {
  padding: 8px 16px;
  background: #409eff;
  color: #fff;
  border-radius: 4px;
  text-decoration: none;
}
</style>
