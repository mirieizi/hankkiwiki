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
    // 랜덤 / 히스토리 / 커스텀 추천 (POST 방식으로 수정)
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
    },

    // AI 추천 (RAG 기반)
    async fetchAiRecommendation(payload) {
      this.loading = true;
      this.showNoHistoryPrompt = false;
      this.showLoginPrompt = false;
      
      try {
        const result = await recommendService.getRagRecommendation(payload);
        this.recommendation = result;
        this.hasRun = true;
      } catch (error) {
        console.error('AI 추천 실패:', error);
        this.handleRecommendError(error, 'ai');
        throw error;
      } finally {
        this.loading = false;
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

    // 최근 3일간의 식단 기록 불러오기
    async fetchHistoryRecords() {
      try {
        const data = await recommendService.getHistoryRecords();
        this.historyRecords = data || [];
        
        // 히스토리가 없으면 프롬프트 표시하지 않음 (기본적으로 false)
        // this.showNoHistoryPrompt = Array.isArray(data) && data.length === 0;
      } catch (e) {
        console.error("fetchHistoryRecords error:", e);
        this.historyRecords = [];
        
        // 인증 오류면 로그인 프롬프트
        if (e.response?.status === 401) {
          this.showLoginPrompt = true;
        }
      }
    },

    // 에러 처리 통합
    handleRecommendError(error, mode) {
      const status = error.response?.status;
      
      if (status === 401) {
        this.showLoginPrompt = true;
      } else if (status === 404) {
        // 히스토리나 커스텀 모드에서 데이터 부족
        if (mode === 'history' || mode === 'custom') {
          this.showNoHistoryPrompt = true;
        }
      } else if (status === 429) {
        alert('추천 가능 횟수를 초과했습니다. 나중에 다시 시도해주세요.');
      } else if (status >= 500) {
        alert('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
      } else {
        alert('추천 중 오류가 발생했습니다.');
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
