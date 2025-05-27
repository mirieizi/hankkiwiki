import axios from '@/plugins/axios';

export const foodService = {
  // 음식 검색 (별도 API 가정)
  async searchFoods(query) {
    try {
      const response = await axios.get('/food/search', {
        params: { q: query }
      });
      return response.data;
    } catch (error) {
      console.error('음식 검색 실패:', error);
      throw error;
    }
  },

  // 식단 생성 (Diet 생성)
  async createDiet(dietData) {
    try {
      const response = await axios.post('/diet', dietData);
      return response.data;
    } catch (error) {
      console.error('식단 생성 실패:', error);
      throw error;
    }
  },

  // 특정 날짜의 식단 조회
  async getDietsByDate(takeAt) {
    try {
      const response = await axios.get('/diet/get-by-date', {
        data: { takeAt: takeAt }
      });
      return response.data;
    } catch (error) {
      console.error('식단 조회 실패:', error);
      throw error;
    }
  },

  // 식단 수정 (meal-type)
  async updateDietMealType(dietUpdateData) {
    try {
      const response = await axios.patch('/diet/update/meal-type', dietUpdateData);
      return response.data;
    } catch (error) {
      console.error('식단 수정 실패:', error);
      throw error;
    }
  },

  // 식단 삭제
  async deleteDiet(dietId) {
    try {
      const response = await axios.delete('/diet/delete', {
        data: { dietId: dietId }
      });
      return response.data;
    } catch (error) {
      console.error('식단 삭제 실패:', error);
      throw error;
    }
  }
};
