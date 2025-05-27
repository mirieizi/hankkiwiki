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

  // 최근 식단 기록 조회 (Diet API 활용) - 수정된 버전
  async getHistoryRecords() {
    try {
      // 최근 3일간 식단 조회
      const today = new Date();
      const dates = [];
      for (let i = 0; i < 3; i++) {
        const date = new Date(today);
        date.setDate(date.getDate() - i);
        dates.push(date.toISOString().split("T")[0]); // YYYY-MM-DD 형식
      }

      const historyPromises = dates.map(async (date) => {
        try {
          // GET 요청에서 params 사용
          const response = await axios.get("/diet/get-by-date", {
            params: { takeAt: date },
          });

          // 응답 데이터 구조 확인 및 처리
          const data = response.data;
          if (Array.isArray(data)) {
            return data.map((item) => ({
              ...item,
              date: date, // 날짜 정보 추가
            }));
          }
          return [];
        } catch (error) {
          console.error(`${date} 식단 조회 실패:`, error);
          return [];
        }
      });

      const results = await Promise.all(historyPromises);
      const flatResults = results.flat();

      console.log("식단 기록 조회 결과:", flatResults); // 디버깅용
      return flatResults;
    } catch (error) {
      console.error("히스토리 조회 실패:", error);
      return [];
    }
  },
};
