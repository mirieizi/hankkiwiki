// src/stores/recommend.js
import { defineStore } from "pinia";
import axios from "@/plugins/axios";

export const useRecommendStore = defineStore("recommend", {
  state: () => ({
    recommendation: null,
    loading: false,
    hasRun: false,

    // recommendation currency
    remainingSpoons: 0,
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
      let url = "";
      switch (mode) {
        case "random":
          url = "/recommend/random";
          break;
        case "history":
          url = "/recommend/furthest";
          break;
        case "custom":
          url = "/recommend/similar";
          break;
        default:
          url = "/recommend/random";
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
        const { data } = await axios.get("/recommend/spoons");
        this.remainingSpoons = data.remainingSpoons;
      } catch (e) {
        console.error("fetchRemainingSpoons error:", e);
        this.remainingSpoons = 0;
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
