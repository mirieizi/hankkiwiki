// src/stores/recommend.js
import { defineStore } from "pinia";
import axios from "@/plugins/axios";
export const useRecommendStore = defineStore("recommend", {
  state: () => ({
    recommendation: null,
    loading: false,
    hasRun: false,
  }),
  actions: {
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
          break;
      }
      const { data } = await axios.get(url);
      this.recommendation = data;
      this.hasRun = true;
      this.loading = false;
    },
    async fetchAiRecommendation(payload) {
      this.loading = true;
      const { data } = await axios.post("/recommend/rag", payload);
      this.recommendation = data;
      this.hasRun = true;
      this.loading = false;
    },
  },
});
