import axios from '@/plugins/axios';

export const calendarService = {
  // 특정 날짜의 식단 조회
  async getDietsByDate(date) {
    console.log('=== getDietsByDate 호출 ===');
    console.log('요청 날짜:', date);
    
    try {
      // ❗ 주의: GET 요청에서 data 대신 params 사용
      const response = await axios.get('/diet/get-by-date', {
        data: { takeAt: date } // 이 부분이 문제일 수 있음
      });
      
      console.log('식단 API 응답:', response.data);
      return response.data || [];
    } catch (error) {
      console.error('식단 조회 실패:', error);
      console.error('에러 상태:', error.response?.status);
      console.error('에러 데이터:', error.response?.data);
      
      if (error.response?.status === 404) {
        return []; // 데이터 없음
      }
      throw error;
    }
  },

  // 전체 일기 목록 조회
  async getAllDiaries() {
    console.log('=== getAllDiaries 호출 ===');
    
    try {
      const response = await axios.get('/user/me/diaries');
      console.log('일기 목록 API 응답:', response.data);
      return response.data || [];
    } catch (error) {
      console.error('일기 목록 조회 실패:', error);
      console.error('에러 상태:', error.response?.status);
      
      if (error.response?.status === 404) {
        return [];
      }
      throw error;
    }
  },

  // 날짜별 일기 찾기
  async getDiaryByDate(date, allDiaries = null) {
    console.log('=== getDiaryByDate 호출 ===');
    console.log('찾을 날짜:', date);
    
    try {
      const diaries = allDiaries || await this.getAllDiaries();
      console.log('전체 일기 목록:', diaries);
      
      // 날짜 형식을 맞춰서 비교
      const targetDiary = diaries.find(diary => {
        if (!diary.date) return false;
        
        // 다양한 날짜 형식 처리
        let diaryDate;
        if (typeof diary.date === 'string') {
          diaryDate = diary.date.split('T')[0]; // ISO 형식에서 날짜 부분만
        } else {
          diaryDate = new Date(diary.date).toISOString().split('T')[0];
        }
        
        console.log(`일기 날짜 비교: ${diaryDate} === ${date}`);
        return diaryDate === date;
      });
      
      console.log('찾은 일기:', targetDiary);
      return targetDiary || null;
    } catch (error) {
      console.error('날짜별 일기 조회 실패:', error);
      return null;
    }
  },

  // 특정 날짜의 통합 데이터 조회
  async getDayRecord(date) {
    console.log('=== getDayRecord 호출 ===');
    console.log('요청 날짜:', date);
    
    try {
      const [diets, diary] = await Promise.all([
        this.getDietsByDate(date),
        this.getDiaryByDate(date)
      ]);

      console.log('조회된 식단:', diets);
      console.log('조회된 일기:', diary);

      const result = {
        date,
        diets: diets || [],
        diary: diary || null,
        hasRecord: (diets && diets.length > 0) || !!diary
      };

      console.log('최종 결과:', result);
      return result;
    } catch (error) {
      console.error('일일 기록 조회 실패:', error);
      return {
        date,
        diets: [],
        diary: null,
        hasRecord: false
      };
    }
  }
};
