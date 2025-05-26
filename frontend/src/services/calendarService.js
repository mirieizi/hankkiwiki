import axios from '@/plugins/axios';

export const calendarService = {
  // 특정 날짜의 식단 조회
  async getDietsByDate(date) {
    try {
      const response = await axios.get('/diet/get-by-date', {
        data: { takeAt: date }
      });
      return response.data || [];
    } catch (error) {
      console.error('식단 조회 실패:', error);
      if (error.response?.status === 404) {
        return []; // 데이터 없음
      }
      throw error;
    }
  },

  // 전체 일기 목록 조회
  async getAllDiaries() {
    try {
      const response = await axios.get('/user/me/diaries');
      return response.data || [];
    } catch (error) {
      console.error('일기 목록 조회 실패:', error);
      if (error.response?.status === 404) {
        return [];
      }
      throw error;
    }
  },

  // 날짜별 일기 찾기 (클라이언트 필터링)
  async getDiaryByDate(date, allDiaries = null) {
    try {
      const diaries = allDiaries || await this.getAllDiaries();
      
      // 날짜 형식을 맞춰서 비교 (YYYY-MM-DD)
      const targetDiary = diaries.find(diary => {
        if (!diary.date) return false;
        
        // diary.date가 LocalDate 형식이라면 문자열로 변환
        const diaryDate = typeof diary.date === 'string' 
          ? diary.date 
          : new Date(diary.date).toISOString().split('T')[0];
        
        return diaryDate === date;
      });
      
      return targetDiary || null;
    } catch (error) {
      console.error('날짜별 일기 조회 실패:', error);
      return null;
    }
  },

  // 월별 기록된 날짜들 조회
  async getRecordedDatesInMonth(year, month) {
    try {
      const [diaries] = await Promise.all([
        this.getAllDiaries()
      ]);

      const recordedDates = new Set();

      // 일기 날짜들 추가
      diaries.forEach(diary => {
        if (diary.date) {
          const diaryDate = typeof diary.date === 'string' 
            ? diary.date 
            : new Date(diary.date).toISOString().split('T')[0];
          
          const [diaryYear, diaryMonth] = diaryDate.split('-').map(Number);
          if (diaryYear === year && diaryMonth === month + 1) {
            recordedDates.add(diaryDate);
          }
        }
      });

      // 식단 날짜들은 개별 조회해야 함 (효율성을 위해 일단 일기만)
      return Array.from(recordedDates);
    } catch (error) {
      console.error('월별 기록 날짜 조회 실패:', error);
      return [];
    }
  },

  // 특정 날짜의 통합 데이터 조회
  async getDayRecord(date) {
    try {
      const [diets, diary] = await Promise.all([
        this.getDietsByDate(date),
        this.getDiaryByDate(date)
      ]);

      return {
        date,
        diets: diets || [],
        diary: diary || null,
        hasRecord: (diets && diets.length > 0) || !!diary
      };
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
