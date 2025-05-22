// src/stores/recommend.js

import { defineStore } from "pinia";
import axios from "@/plugins/axios";

export const useRecommendStore = defineStore("recommend", {
  state: () => ({
    remainingSpoons: 0,
    loading: false,
    recommendation: null,
    hasRun: false,
    runCount: 0,
    historyRecords: [],
    showNoHistoryPrompt: false,
    showLoginPrompt: false,
  }),
  getters: {
    spoonCount: (state) => state.remainingSpoons,
    result: (state) => state.recommendation,
    expanded: (state) => state.loading || !!state.recommendation,
  },
  actions: {
    requireLogin() {
      if (!localStorage.getItem("accessToken")) {
        this.showLoginPrompt = true;
        return false;
      }
      return true;
    },
    async fetchRemainingSpoons() {
      try {
        const { data } = await axios.get("/recommend/remaining");
        this.remainingSpoons = data.count;
      } catch (e) {
        if (e.response?.status === 401) this.showLoginPrompt = true;
        else console.error("Remaining fetch 실패:", e);
      }
    },
    async fetchRecommendation(mode) {
      try {
        this.loading = true;
        const { data } = await axios.post("/recommend", null, { params: { mode } });
        this.recommendation = data;
        await this.fetchRemainingSpoons();
        this.hasRun = true;
        this.runCount++;
      } catch (e) {
        if (e.response?.status === 401) this.showLoginPrompt = true;
        else console.error("Recommendation fetch 실패:", e);
      } finally {
        this.loading = false;
      }
    },
    async fetchHistoryRecords() {
      try {
        const today = new Date().toISOString().split("T")[0];
        const startDate = new Date(Date.now() - 2 * 864e5).toISOString().split("T")[0];
        const { data } = await axios.get("/diet", { params: { startDate, endDate: today } });
        this.historyRecords = data;
      } catch {
        this.historyRecords = [];
      }
    },
    resetRecommend() {
      this.recommendation = null;
      this.hasRun = false;
      this.runCount = 0;
    },
    showNoHistory() {
      this.showNoHistoryPrompt = true;
    },
    hideNoHistory() {
      this.showNoHistoryPrompt = false;
    },
    showLogin() {
      this.showLoginPrompt = true;
    },
    hideLogin() {
      this.showLoginPrompt = false;
    },
  },
});
