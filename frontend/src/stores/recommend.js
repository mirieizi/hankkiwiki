import { defineStore } from "pinia";
import { recommendService } from "@/services/recommendService";

export const useRecommendStore = defineStore("recommend", {
  state: () => ({
    recommendation: null,
    loading: false,
    hasRun: false,

    // recommendation currency
    remainingSpoons: 5,
    runCount: 0,

    // for history-based prompt
    historyRecords: [],
    showNoHistoryPrompt: false,
    showLoginPrompt: false,

    // UI expansion flag (추천 카드 확장 등)
    expanded: false,
  }),

  actions: {
    // 랜덤 / 히스토리 / 커스텀 추천 (추천 후 스푼 동기화)
    async fetchRecommendation(mode) {
      this.loading = true;
      this.showNoHistoryPrompt = false;
      this.showLoginPrompt = false;
      
      try {
        let result;
        
        switch (mode) {
          case "random":
            result = await recommendService.getRandomRecommendation();
            break;
          case "history":
            result = await recommendService.getFurthestRecommendation();
            break;
          case "custom":
            result = await recommendService.getSimilarRecommendation();
            break;
          default:
            result = await recommendService.getRandomRecommendation();
        }
        
        this.recommendation = result;
        this.hasRun = true;
        
      } catch (error) {
        console.error('추천 실패:', error);
        this.handleRecommendError(error, mode);
        throw error;
      } finally {
        this.loading = false;
      }
      try {
        const { data } = await axios.get(url);
        this.recommendation = data;
        this.hasRun = true;
      } finally {
        this.loading = false;
        // 추천 끝나고 스푼 동기화
        await this.fetchRemainingSpoons();
      }
    },

    // AI 추천 (RAG 기반, 추천 후 스푼 동기화)
    async fetchAiRecommendation(payload) {
      this.loading = true;
      try {
        const { data } = await axios.post("/recommend/rag", payload);
        this.recommendation = data;
        this.hasRun = true;
      } finally {
        this.loading = false;
        // 추천 끝나고 스푼 동기화
        await this.fetchRemainingSpoons();
      }
    },

    // 남은 스푼 개수 조회
    async fetchRemainingSpoons() {
      try {
        this.remainingSpoons = await recommendService.getSpoonCount();
      } catch (e) {
        console.error("fetchRemainingSpoons error:", e);
        this.remainingSpoons = 5; // 기본값
      }
    },

    // 스푼 사용
    async useSpoons(count) {
      try {
        const result = await recommendService.useSpoons(count);
        this.remainingSpoons = result.remainingSpoons;
      } catch (error) {
        console.error('스푼 사용 실패:', error);
        // 실패해도 UI에서는 차감 (낙관적 업데이트)
        this.remainingSpoons = Math.max(0, this.remainingSpoons - count);
      }
    },

    // 최근 3일간의 식단 기록 유무(T/F)로 받음
    async fetchHistoryRecords() {
      try {
        const { data } = await axios.get("/diet/history-records");
        // data === true or false
        this.showNoHistoryPrompt = !data; // 기록 없으면 true(프롬프트 노출)
      } catch (e) {
        console.error("fetchHistoryRecords error:", e);
        // 네트워크/예외 시에는 일단 "없음" 처리
        this.showNoHistoryPrompt = true;
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
