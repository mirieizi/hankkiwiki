import axios from '@/plugins/axios';

export const recommendService = {
  // 랜덤 추천 (POST 방식)
  async getRandomRecommendation() {
    try {
      const response = await axios.post('/recommend/random');
      return response.data;
    } catch (error) {
      console.error('랜덤 추천 실패:', error);
      throw error;
    }
  },

  // 가장 먼 음식 추천 (새로운 맛) (POST 방식)
  async getFurthestRecommendation() {
    try {
      const response = await axios.post('/recommend/furthest');
      return response.data;
    } catch (error) {
      console.error('새로운 맛 추천 실패:', error);
      throw error;
    }
  },

  // 유사한 음식 추천 (취향 맞춤) (POST 방식)
  async getSimilarRecommendation() {
    try {
      const response = await axios.post('/recommend/similar');
      return response.data;
    } catch (error) {
      console.error('취향 맞춤 추천 실패:', error);
      throw error;
    }
  },

  // AI RAG 기반 추천 (POST 방식)
  async getRagRecommendation(payload = {}) {
    try {
      const response = await axios.post('/recommend/rag', payload);
      return response.data;
    } catch (error) {
      console.error('AI 추천 실패:', error);
      throw error;
    }
  },

  // 사용자 스푼 개수 조회 (임시 - 실제 API 구현 필요)
  async getSpoonCount() {
    try {
      // 임시로 사용자 정보에서 스푼 정보를 가져온다고 가정
      // const response = await axios.get('/user/me/profile');
      // return response.data.remainingSpoons || 5;
      
      // 실제 API가 없으므로 로컬스토리지 활용
      const storedSpoons = localStorage.getItem('userSpoons');
      return storedSpoons ? parseInt(storedSpoons) : 5;
    } catch (error) {
      console.error('스푼 개수 조회 실패:', error);
      return 5; // 기본값
    }
  },

  // 스푼 사용 (차감) - 임시 구현
  async useSpoons(count) {
    try {
      // 실제 API 구현 시:
      // const response = await axios.post('/user/me/spoons/use', { count });
      // return response.data;
      
      // 임시로 로컬스토리지 활용
      const currentSpoons = await this.getSpoonCount();
      const newSpoons = Math.max(0, currentSpoons - count);
      localStorage.setItem('userSpoons', newSpoons.toString());
      return { remainingSpoons: newSpoons };
    } catch (error) {
      console.error('스푼 사용 실패:', error);
      throw error;
    }
  },

  // 최근 식단 기록 조회 (Diet API 활용)
  async getHistoryRecords() {
    try {
      // 최근 3일간 식단 조회
      const today = new Date();
      const dates = [];
      for (let i = 0; i < 3; i++) {
        const date = new Date(today);
        date.setDate(date.getDate() - i);
        dates.push(date.toISOString().split('T')[0]);
      }
      
      const historyPromises = dates.map(async (date) => {
        try {
          const response = await axios.get('/diet/get-by-date', {
            data: { takeAt: date }
          });
          return response.data || [];
        } catch (error) {
          return [];
        }
      });
      
      const results = await Promise.all(historyPromises);
      return results.flat();
    } catch (error) {
      console.error('히스토리 조회 실패:', error);
      return [];
    }
  }
};
