import { defineStore } from "pinia";
// ✅ 수정: 동적 import로 변경하거나 getter에서 제거
// import { recommendService } from "@/services/recommendService";

export const useRecommendStore = defineStore("recommend", {
  state: () => ({
    recommendation: null,
    loading: false,
    hasRun: false,
    currentMode: "random",

    // recommendation currency
    remainingSpoons: 5,
    runCount: 0,

    // for history-based prompt
    historyRecords: [],
    showNoHistoryPrompt: false,
    showLoginPrompt: false,

    // UI expansion flag
    expanded: false,

    // 동적 모드 설정
    availableModes: [
      { mode: "random", label: "랜덤 추천", cost: 1, icon: "🎲", description: "무작위로 음식을 추천합니다" },
      { mode: "custom", label: "새로운 맛", cost: 2, icon: "🌟", description: "평소와 다른 새로운 맛을 추천합니다" },
      { mode: "history", label: "취향 맞춤", cost: 2, icon: "❤️", description: "최근 식단을 바탕으로 취향에 맞는 음식을 추천합니다" },
      { mode: "ai", label: "AI 추천", cost: 3, icon: "🤖", description: "AI가 건강정보와 선호도를 고려해 추천합니다" },
    ],
  }),

  getters: {
    // ✅ 수정: service 호출 제거하고 state에서 직접 조회
    getCurrentModeConfig: (state) => {
      const config = state.availableModes.find((mode) => mode.mode === state.currentMode);
      return (
        config || {
          label: "랜덤 추천",
          description: "무작위로 음식을 추천합니다",
          cost: 1,
          icon: "🎲",
        }
      );
    },

    canAffordRecommendation: (state) => {
      const config = state.availableModes.find((mode) => mode.mode === state.currentMode);
      return state.remainingSpoons >= (config?.cost || 1);
    },
  },

  actions: {
    // ✅ 수정: 이미 state에서 초기화되므로 단순화
    async initializeModes() {
      console.log("모드 초기화 완료 (이미 state에서 설정됨)");
    },

    // 현재 모드 변경
    setCurrentMode(mode) {
      this.currentMode = mode;
      this.resetRecommend();
    },

    // ✅ 수정: 동적 import 사용
    async fetchRecommendation(mode = null, payload = null) {
      const targetMode = mode || this.currentMode;
      this.loading = true;
      this.showNoHistoryPrompt = false;
      this.showLoginPrompt = false;

      try {
        // 동적 import로 service 로드
        const { recommendService } = await import("@/services/recommendService");

        let result;

        switch (targetMode) {
          case "random":
            result = await recommendService.getRandomRecommendation();
            break;
          case "custom":
            result = await recommendService.getFurthestRecommendation();
            break;
          case "history":
            result = await recommendService.getSimilarRecommendation();
            break;
          case "ai":
            result = await recommendService.getRagRecommendation(payload);
            break;
          default:
            result = await recommendService.getRandomRecommendation();
        }

        this.recommendation = result;
        this.hasRun = true;
        this.runCount++;
        this.currentMode = targetMode;

        await this.fetchRemainingSpoons();
      } catch (error) {
        console.error("추천 실패:", error);
        this.handleRecommendError(error, targetMode);
        throw error;
      } finally {
        this.loading = false;
      }
    },

    // AI 추천 (기존 호환성 유지)
    async fetchAiRecommendation(payload) {
      return this.fetchRecommendation("ai", payload);
    },

    // ✅ 수정: 동적 import 사용
    async fetchRemainingSpoons() {
      try {
        const { recommendService } = await import("@/services/recommendService");
        const response = await recommendService.getSpoons();
        this.remainingSpoons = response.remainingSpoons;
      } catch (e) {
        console.error("fetchRemainingSpoons error:", e);
        this.remainingSpoons = 5;
      }
    },

    // 스푼 사용
    async useSpoons(count) {
      console.log(`스푼 ${count}개 사용됨 (백엔드에서 자동 처리)`);
    },

    // ✅ 수정: 동적 import 사용
    async fetchHistoryRecords() {
      try {
        const { recommendService } = await import("@/services/recommendService");
        const data = await recommendService.getHistoryRecords();
        this.historyRecords = data || [];

        console.log("Store에 저장된 식단 기록:", this.historyRecords);

        if (Array.isArray(data) && data.length === 0) {
          this.showNoHistoryPrompt = true;
        }
      } catch (e) {
        console.error("fetchHistoryRecords error:", e);
        this.historyRecords = [];

        if (e.response?.status === 401) {
          this.showLoginPrompt = true;
        } else {
          this.showNoHistoryPrompt = true;
        }
      }
    },

    // 에러 처리
    handleRecommendError(error, mode) {
      const status = error.response?.status;

      if (status === 401) {
        this.showLoginPrompt = true;
      } else if (status === 404) {
        if (mode === "history" || mode === "custom") {
          this.showNoHistoryPrompt = true;
        }
      } else if (status === 429) {
        alert("추천 가능 횟수를 초과했습니다. 나중에 다시 시도해주세요.");
      } else if (status >= 500) {
        alert("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
      } else {
        alert("추천 중 오류가 발생했습니다.");
      }
    },

    // 상태 초기화
    resetRecommend() {
      this.recommendation = null;
      this.hasRun = false;
      this.runCount = 0;
      this.showNoHistoryPrompt = false;
      this.showLoginPrompt = false;
      this.expanded = false;
    },
  },
});
