<template>
  <div class="diary-container">
    <div class="diary-header">
      <p class="date">{{ formattedDate }}</p>
      <div class="user">
        <img src="@/assets/logo.png" alt="프로필" class="profile-image" />
        <span>{{ userName }} 님</span>
      </div>
    </div>

    <p class="label">아래에 일기를 작성해주세요</p>

    <div class="diary-input-section">
      <textarea 
        v-model="diaryContent" 
        class="diary-textarea" 
        placeholder="오늘 하루는 어땠나요?&#10;먹은 음식은 어땠나요?&#10;기분이나 느낀 점을 자유롭게 적어보세요!"
        :disabled="loading"
      ></textarea>
      
      <div class="character-count">
        {{ diaryContent.length }} / 1000자
      </div>
    </div>

    <div class="button-group">
      <button 
        class="load-button" 
        @click="loadExistingDiary" 
        :disabled="loading"
        v-if="!hasLoadedOnce"
      >
        기존 일기 불러오기
      </button>
      
      <button 
        class="submit-button" 
        @click="submitDiary" 
        :disabled="loading || !diaryContent.trim() || diaryContent.length > 1000"
      >
        {{ loading ? "처리 중..." : isEditMode ? "일기 수정하기" : "일기 등록하기" }}
      </button>
    </div>

    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { format, parseISO } from 'date-fns';
import { ko } from 'date-fns/locale';
import { diaryService } from '@/services/diaryService';

const props = defineProps({
  date: { type: String, required: true },
  userName: { type: String, required: true },
});

const emit = defineEmits(['diary-saved']);

const diaryContent = ref('');
const loading = ref(false);
const isEditMode = ref(false);
const hasLoadedOnce = ref(false);
const successMessage = ref('');
const currentDiaryId = ref(null);
const router = useRouter();

// 포맷된 날짜
const formattedDate = computed(() => {
  try {
    const dateObj = parseISO(props.date);
    return format(dateObj, 'yyyy년 MM월 dd일 EEEE', { locale: ko });
  } catch (error) {
    return props.date;
  }
});

// 컴포넌트 마운트 시 기존 일기 자동 로드
onMounted(async () => {
  await loadExistingDiary();
});

// 날짜가 변경될 때마다 일기 다시 로드
watch(() => props.date, async () => {
  hasLoadedOnce.value = false;
  isEditMode.value = false;
  currentDiaryId.value = null;
  await loadExistingDiary();
});

// 기존 일기 로드
async function loadExistingDiary() {
  if (hasLoadedOnce.value) return;
  
  loading.value = true;
  
  try {
    const diary = await diaryService.getDiaryByDate(props.date);
    if (diary && diary.content) {
      diaryContent.value = diary.content;
      currentDiaryId.value = diary.id;
      isEditMode.value = true;
    } else {
      diaryContent.value = '';
      currentDiaryId.value = null;
      isEditMode.value = false;
    }
  } catch (error) {
    if (error.response?.status === 404) {
      // 일기가 없는 경우는 정상적인 상황
      diaryContent.value = '';
      isEditMode.value = false;
      currentDiaryId.value = null;
    } else if (error.response?.status === 401) {
      alert('로그인이 필요합니다.');
      router.push('/login');
      return;
    } else {
      console.error('기존 일기 로드 실패:', error);
    }
  } finally {
    loading.value = false;
    hasLoadedOnce.value = true;
  }
}

// 일기 등록/수정
async function submitDiary() {
  if (!diaryContent.value.trim()) {
    alert('일기 내용을 입력해주세요.');
    return;
  }

  if (diaryContent.value.length > 1000) {
    alert('일기는 1000자 이내로 작성해주세요.');
    return;
  }

  const action = isEditMode.value ? '수정' : '등록';
  const confirmed = confirm(`일기를 ${action}하시겠습니까?`);
  if (!confirmed) return;

  loading.value = true;
  successMessage.value = '';

  try {
    if (isEditMode.value && currentDiaryId.value) {
      // 수정
      const result = await diaryService.updateDiary(currentDiaryId.value, {
        content: diaryContent.value,
        date: props.date
      });
      successMessage.value = '일기가 성공적으로 수정되었습니다!';
    } else {
      // 새로 생성
      const result = await diaryService.createDiary({
        content: diaryContent.value,
        date: props.date
      });
      currentDiaryId.value = result;
      isEditMode.value = true;
      successMessage.value = '일기가 성공적으로 등록되었습니다!';
    }
    
    emit('diary-saved');
    
    // 성공 메시지 표시 후 자동으로 사라지게
    setTimeout(() => {
      successMessage.value = '';
    }, 3000);
    
  } catch (error) {
    console.error('일기 저장 실패:', error);
    
    if (error.response?.status === 401) {
      alert('로그인이 필요합니다.');
      router.push('/login');
    } else if (error.response?.status === 400) {
      alert('잘못된 요청입니다. 내용을 확인해주세요.');
    } else if (error.response?.status >= 500) {
      alert('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
    } else {
      alert(`일기 ${action} 중 오류가 발생했습니다.`);
    }
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.diary-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 100%;
  width: 100%;
}

.diary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 1rem;
  border-bottom: 2px solid #fff0c1;
}

.date {
  font-weight: bold;
  font-size: 1.3rem;
  color: #333;
}

.user {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.profile-image {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #ffd983;
}

.label {
  margin-bottom: 0.5rem;
  font-size: 1rem;
  color: #555;
  font-weight: 500;
}

.diary-input-section {
  position: relative;
}

.diary-textarea {
  width: 100%;
  min-height: 300px;
  padding: 1.5rem;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  resize: vertical;
  font-size: 1rem;
  font-family: inherit;
  line-height: 1.6;
  transition: border-color 0.2s ease;
  box-sizing: border-box;
}

.diary-textarea:focus {
  outline: none;
  border-color: #ffd983;
}

.diary-textarea:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.character-count {
  text-align: right;
  margin-top: 0.5rem;
  font-size: 0.9rem;
  color: #666;
}

.button-group {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.load-button {
  background-color: #2196f3;
  color: white;
  border: none;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
  flex: 1;
  min-width: 150px;
  font-size: 1rem;
}

.load-button:hover:not(:disabled) {
  background-color: #1976d2;
}

.submit-button {
  background-color: #ff9800;
  color: white;
  border: none;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
  flex: 2;
  min-width: 200px;
  font-size: 1rem;
}

.submit-button:hover:not(:disabled) {
  background-color: #f57c00;
  transform: translateY(-1px);
}

.load-button:disabled,
.submit-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.success-message {
  background-color: #e8f5e8;
  color: #2e7d32;
  padding: 1rem;
  border-radius: 8px;
  text-align: center;
  font-weight: 500;
  border: 1px solid #c8e6c9;
}

@media screen and (max-width: 640px) {
  .button-group {
    flex-direction: column;
  }
  
  .load-button,
  .submit-button {
    flex: 1;
    min-width: unset;
  }
  
  .diary-textarea {
    min-height: 250px;
    padding: 1rem;
  }
}
</style>
