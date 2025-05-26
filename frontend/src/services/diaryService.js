import axios from '@/plugins/axios';

export const diaryService = {
  // 일기 작성
  async createDiary(diaryData) {
    try {
      const response = await axios.post('/user/me/diaries', diaryData);
      return response.data;
    } catch (error) {
      console.error('일기 작성 실패:', error);
      throw error;
    }
  },

  // 전체 일기 목록 조회
  async getAllDiaries() {
    try {
      const response = await axios.get('/user/me/diaries');
      return response.data;
    } catch (error) {
      console.error('일기 목록 조회 실패:', error);
      throw error;
    }
  },

  // 특정 일기 조회 (ID로)
  async getDiaryById(diaryId) {
    try {
      const response = await axios.get(`/user/me/diaries/${diaryId}`);
      return response.data;
    } catch (error) {
      console.error('일기 조회 실패:', error);
      throw error;
    }
  },

  // 특정 날짜 일기 조회 (날짜로 찾기)
  async getDiaryByDate(date) {
    try {
      const diaries = await this.getAllDiaries();
      // 클라이언트에서 날짜로 필터링 (백엔드에 날짜별 조회 API가 없으므로)
      const targetDiary = diaries.find(diary => {
        const diaryDate = new Date(diary.createdAt).toISOString().split('T')[0];
        return diaryDate === date;
      });
      return targetDiary || null;
    } catch (error) {
      console.error('날짜별 일기 조회 실패:', error);
      throw error;
    }
  },

  // 일기 수정
  async updateDiary(diaryId, updateData) {
    try {
      const response = await axios.patch(`/user/me/diaries/${diaryId}`, updateData);
      return response.data;
    } catch (error) {
      console.error('일기 수정 실패:', error);
      throw error;
    }
  },

  // 일기 삭제
  async deleteDiary(diaryId) {
    try {
      const response = await axios.delete(`/user/me/diaries/${diaryId}`);
      return response.data;
    } catch (error) {
      console.error('일기 삭제 실패:', error);
      throw error;
    }
  }
};
