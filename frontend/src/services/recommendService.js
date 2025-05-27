import axios from "@/plugins/axios";

// 추천 모드별 설정
const RECOMMEND_CONFIG = {
  random: {
    endpoint: "/recommend/random",
    label: "랜덤 추천",
    description: "무작위로 음식을 추천합니다",
    cost: 1,
    icon: "🎲",
  },
  custom: {
    endpoint: "/recommend/furthest",
    label: "새로운 맛",
    description: "평소와 다른 새로운 맛을 추천합니다",
    cost: 2,
    icon: "🌟",
  },
  history: {
    endpoint: "/recommend/similar",
    label: "취향 맞춤",
    description: "최근 식단을 바탕으로 취향에 맞는 음식을 추천합니다",
    cost: 2,
    icon: "❤️",
  },
  ai: {
    endpoint: "/recommend/rag",
    label: "AI 추천",
    description: "AI가 건강정보와 선호도를 고려해 추천합니다",
    cost: 3,
    icon: "🤖",
  },
};

export const recommendService = {
  // 추천 모드 설정 조회
  getRecommendConfig(mode) {
    return RECOMMEND_CONFIG[mode] || RECOMMEND_CONFIG.random;
  },

  // 모든 추천 모드 목록 조회
  getAllRecommendModes() {
    return Object.keys(RECOMMEND_CONFIG).map((mode) => ({
      mode,
      ...RECOMMEND_CONFIG[mode],
    }));
  },

  // 동적 추천 API 호출
  async getRecommendation(mode, payload = null) {
    const config = this.getRecommendConfig(mode);

    try {
      let response;

      if (mode === "ai" && payload) {
        response = await axios.post(config.endpoint, payload);
      } else {
        response = await axios.post(config.endpoint);
      }

      return response.data;
    } catch (error) {
      console.error(`${config.label} 실패:`, error);
      throw error;
    }
  },

  // 기존 메서드들을 동적 호출로 리팩토링
  async getRandomRecommendation() {
    return this.getRecommendation("random");
  },

  async getFurthestRecommendation() {
    return this.getRecommendation("custom");
  },

  async getSimilarRecommendation() {
    return this.getRecommendation("history");
  },

  async getRagRecommendation(payload) {
    return this.getRecommendation("ai", payload);
  },

  // 스푼 관련 API
  async getSpoons() {
    try {
      const response = await axios.get("/recommend/spoons");
      return response.data;
    } catch (error) {
      console.error("스푼 개수 조회 실패:", error);
      const storedSpoons = localStorage.getItem("userSpoons");
      return { remainingSpoons: storedSpoons ? parseInt(storedSpoons) : 5 };
    }
  },

  // 최근 식단 기록 **존재 여부**만 boolean으로 반환 (t/f)
  async getHistoryRecords() {
    try {
      // 백엔드에서 true/false 반환한다고 가정!
      const response = await axios.get("/diet/history-records");
      return !!response.data; // t/f 보장 (혹시라도 undefined/null일 때 false)
    } catch (error) {
      console.error("히스토리 존재여부 조회 실패:", error);
      // 에러 시엔 일단 false(식단 없음)로 간주
      return false;
    }
  },
};
