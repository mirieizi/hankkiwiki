import axios from "@/plugins/axios";

export const foodService = {
  // 음식 검색 (별도 API 가정)
  async searchFoods(query) {
    try {
      const response = await axios.get("/food/search", {
        params: { query: query },
      });
      return response.data;
    } catch (error) {
      console.error("음식 검색 실패:", error);
      throw error;
    }
  },

  // ✅ 음식 상세 정보 (새로운 API)
  async getFoodDetail(foodId) {
    try {
      const response = await axios.get(`/food/${foodId}`);
      return response.data; // FoodResponseDto
    } catch (error) {
      console.error("음식 상세 정보 조회 실패:", error);
      throw error;
    }
  },

  // ✅ 음식 이름으로 정확히 찾기 (새로운 API)
  async getFoodByName(foodName) {
    try {
      const response = await axios.get("/food/name", {
        params: { foodName: foodName },
      });
      return response.data; // FoodResponseDto
    } catch (error) {
      console.error("음식 이름 검색 실패:", error);
      throw error;
    }
  },

  // ✅ 사용자 최근 음식 조회 (새로운 API)
  async getRecentFoods() {
    try {
      const response = await axios.get("/food/recent");
      return response.data; // List<FoodPreviewResponseDto>
    } catch (error) {
      console.error("최근 음식 조회 실패:", error);
      throw error;
    }
  },

  // 식단 생성 (Diet 생성)
  async createDiet(dietData) {
    try {
      const response = await axios.post("/diet", dietData);
      return response.data;
    } catch (error) {
      console.error("식단 생성 실패:", error);
      throw error;
    }
  },

  async getDietsByDate(takeAt) {
    try {
      const response = await axios.get("/diet/get-by-date", {
        params: { takeAt: takeAt },
      });
      return response.data || [];
    } catch (error) {
      console.error("식단 조회 실패:", error);
      if (error.response?.status === 404) {
        return [];
      }
      throw error;
    }
  },

  // 식단 수정 (meal-type)
  async updateDietMealType(dietUpdateData) {
    try {
      const response = await axios.patch("/diet/update/meal-type", dietUpdateData);
      return response.data;
    } catch (error) {
      console.error("식단 수정 실패:", error);
      throw error;
    }
  },

  // 식단 삭제
  async deleteDiet(dietId) {
    try {
      // 방법 1: POST 요청으로 변경 (권장)
      const response = await axios.post("/diet/delete", {
        dietId: dietId,
      });
      return response.data;
    } catch (error) {
      console.error("식단 삭제 실패:", error);
      throw error;
    }
  },
};
