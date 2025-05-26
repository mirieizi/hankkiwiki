import axios from '@/plugins/axios';

export const calendarService = {
  // ✅ 수정된 식단 조회 (GET + RequestParam 방식)
  async getDietsByDate(date) {
    console.log('=== getDietsByDate 호출 ===');
    console.log('요청 날짜:', date);
    
    try {
      // GET /api/diet/get-by-date?takeAt=2025-05-27
      const response = await axios.get('/diet/get-by-date', {
        params: { 
          takeAt: date  // LocalDate 파라미터로 전송
        }
      });
      
      console.log('식단 API 응답:', response.data);
      console.log('응답 상태:', response.status);
      
      return response.data || [];
    } catch (error) {
      console.error('❌ 식단 조회 실패:', error);
      console.error('에러 상태:', error.response?.status);
      console.error('에러 데이터:', error.response?.data);
      
      if (error.response?.status === 404) {
        console.log('ℹ️ 해당 날짜에 식단 데이터 없음');
        return [];
      }
      if (error.response?.status === 401) {
        throw new Error('인증이 필요합니다');
      }
      throw error;
    }
  },

  // 전체 일기 목록 조회 (이미 수정됨)
  async getAllDiaries() {
    console.log('=== getAllDiaries 호출 ===');
    
    try {
      const response = await axios.get('/user/me/diaries');
      console.log('일기 목록 API 응답:', response.data);
      console.log('응답 상태:', response.status);
      console.log('일기 개수:', response.data?.length || 0);
      
      return response.data || [];
    } catch (error) {
      console.error('❌ 일기 목록 조회 실패:', error);
      console.error('에러 상태:', error.response?.status);
      console.error('에러 데이터:', error.response?.data);
      
      if (error.response?.status === 404) {
        console.log('ℹ️ 일기 데이터 없음');
        return [];
      }
      if (error.response?.status === 401) {
        throw new Error('인증이 필요합니다');
      }
      throw error;
    }
  },

  // 날짜별 일기 찾기 (클라이언트 필터링)
  async getDiaryByDate(date, allDiaries = null) {
    console.log('=== getDiaryByDate 호출 ===');
    console.log('찾을 날짜:', date);
    
    try {
      const diaries = allDiaries || await this.getAllDiaries();
      console.log('전체 일기 목록에서 검색:', diaries);
      
      const targetDiary = diaries.find(diary => {
        if (!diary.date) return false;
        
        let diaryDate;
        if (typeof diary.date === 'string') {
          diaryDate = diary.date.split('T')[0];
        } else if (Array.isArray(diary.date)) {
          const [year, month, day] = diary.date;
          diaryDate = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        } else {
          diaryDate = new Date(diary.date).toISOString().split('T')[0];
        }
        
        console.log(`날짜 비교: ${diaryDate} === ${date}`);
        return diaryDate === date;
      });
      
      console.log('찾은 일기:', targetDiary);
      return targetDiary || null;
    } catch (error) {
      console.error('❌ 날짜별 일기 조회 실패:', error);
      throw error;
    }
  },

  // 월별 기록된 날짜들 조회 (간소화)
  async getRecordedDatesInMonth(year, month) {
    console.log('=== getRecordedDatesInMonth 호출 ===');
    console.log('요청 년월:', year, month);
    
    try {
      const [diaries] = await Promise.all([
        this.getAllDiaries()
      ]);

      const recordedDates = new Set();

      // 일기 날짜들 추가
      diaries.forEach(diary => {
        if (diary.date) {
          let diaryDate;
          if (typeof diary.date === 'string') {
            diaryDate = diary.date.split('T')[0];
          } else if (Array.isArray(diary.date)) {
            const [diaryYear, diaryMonth, diaryDay] = diary.date;
            diaryDate = `${diaryYear}-${String(diaryMonth).padStart(2, '0')}-${String(diaryDay).padStart(2, '0')}`;
          } else {
            diaryDate = new Date(diary.date).toISOString().split('T')[0];
          }
          
          const [diaryYear, diaryMonth] = diaryDate.split('-').map(Number);
          if (diaryYear === year && diaryMonth === month + 1) {
            recordedDates.add(diaryDate);
          }
        }
      });

      console.log('월별 기록된 날짜들:', Array.from(recordedDates));
      return Array.from(recordedDates);
    } catch (error) {
      console.error('월별 기록 날짜 조회 실패:', error);
      return [];
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

      console.log('✅ 조회된 식단:', diets);
      console.log('✅ 조회된 일기:', diary);

      const result = {
        date,
        diets: diets || [],
        diary: diary || null,
        hasRecord: (diets && diets.length > 0) || !!diary
      };

      console.log('📊 최종 결과:', result);
      console.log('📊 hasRecord:', result.hasRecord);
      
      return result;
    } catch (error) {
      console.error('❌ 일일 기록 조회 실패:', error);
      
      if (error.message?.includes('인증이 필요')) {
        throw error;
      }
      
      return {
        date,
        diets: [],
        diary: null,
        hasRecord: false
      };
    }
  }
};
