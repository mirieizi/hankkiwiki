import axios from '@/plugins/axios';

export const diaryService = {
  // 일기 작성 (DiaryCreateRequest)
  async createDiary(diaryData) {
    try {
      const requestData = {
        date: diaryData.date, // LocalDate (YYYY-MM-DD)
        content: diaryData.content // String, 최대 200자
      };
      
      console.log('일기 생성 요청:', requestData);
      
      // POST /api/user/me/diaries
      const response = await axios.post('/user/me/diaries', requestData);
      console.log('일기 생성 응답:', response.data);
      
      return response.data; // Long 타입의 diaryId 반환
    } catch (error) {
      console.error('일기 작성 실패:', error);
      
      if (error.response?.status === 400) {
        const errorMessage = error.response.data?.message || '입력값이 올바르지 않습니다.';
        throw new Error(errorMessage);
      }
      throw error;
    }
  },

  // 전체 일기 목록 조회
  async getAllDiaries() {
    try {
      // GET /api/user/me/diaries
      const response = await axios.get('/user/me/diaries');
      console.log('일기 목록 조회 응답:', response.data);
      return response.data || []; // List<DiaryResponse>
    } catch (error) {
      console.error('일기 목록 조회 실패:', error);
      if (error.response?.status === 404) {
        return [];
      }
      throw error;
    }
  },

  // 특정 일기 조회 (ID로)
  async getDiaryById(diaryId) {
    try {
      // GET /api/user/me/diaries/{diaryId}
      const response = await axios.get(`/user/me/diaries/${diaryId}`);
      console.log('일기 상세 조회 응답:', response.data);
      return response.data; // DiaryResponse
    } catch (error) {
      console.error('일기 조회 실패:', error);
      throw error;
    }
  },

  // 특정 날짜 일기 조회 (DiaryResponse 구조 기준)
  async getDiaryByDate(date) {
    try {
      const diaries = await this.getAllDiaries();
      console.log('전체 일기 목록에서 날짜 필터링:', diaries);
      
      const targetDiary = diaries.find(diary => {
        if (!diary.date) return false;
        
        let diaryDate;
        if (typeof diary.date === 'string') {
          diaryDate = diary.date.split('T')[0]; // ISO 날짜에서 날짜 부분만
        } else if (Array.isArray(diary.date)) {
          // LocalDate가 [2025, 5, 28] 형태로 올 경우
          const [year, month, day] = diary.date;
          diaryDate = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        } else {
          diaryDate = new Date(diary.date).toISOString().split('T')[0];
        }
        
        console.log(`날짜 비교: ${diaryDate} === ${date}`);
        return diaryDate === date;
      });
      
      console.log('찾은 일기:', targetDiary);
      return targetDiary || null; // DiaryResponse 또는 null
    } catch (error) {
      console.error('날짜별 일기 조회 실패:', error);
      throw error;
    }
  },

  // 일기 수정 (DiaryUpdateRequest)
  async updateDiary(diaryId, updateData) {
    try {
      const requestData = {
        content: updateData.content, // 필수
        ...(updateData.date && { date: updateData.date }) // 선택적
      };
      
      console.log('일기 수정 요청:', requestData);
      
      // PATCH /api/user/me/diaries/{diaryId}
      const response = await axios.patch(`/user/me/diaries/${diaryId}`, requestData);
      console.log('일기 수정 응답:', response.data);
      
      return response.data; // DiaryResponse
    } catch (error) {
      console.error('일기 수정 실패:', error);
      
      if (error.response?.status === 400) {
        const errorMessage = error.response.data?.message || '입력값이 올바르지 않습니다.';
        throw new Error(errorMessage);
      }
      throw error;
    }
  },

  // 일기 삭제
  async deleteDiary(diaryId) {
    try {
      console.log('일기 삭제 요청:', diaryId);
      
      // DELETE /api/user/me/diaries/{diaryId}
      const response = await axios.delete(`/user/me/diaries/${diaryId}`);
      console.log('일기 삭제 완료');
      return response.data;
    } catch (error) {
      console.error('일기 삭제 실패:', error);
      throw error;
    }
  }
};
