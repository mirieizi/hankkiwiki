<!-- src/components/registerFood/DiaryEditor.vue -->
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

    <textarea
      v-model="diaryContent"
      class="diary-textarea"
      placeholder="오늘 하루는 어땠나요?"
    ></textarea>

    <button class="submit-button" @click="submitDiary">등록하기</button>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { format, parseISO } from 'date-fns';
import { ko } from 'date-fns/locale';

// props
const props = defineProps({
  date: {
    type: String,
    required: true,
  },
  userName: {
    type: String,
    required: true,
  },
});

const diaryContent = ref('');

// emit
const emit = defineEmits(['submit']);

const formattedDate = computed(() => {
  if (!props.date) return '';
  const dateObj = parseISO(props.date); // ✅ 문자열 → Date 객체로 변환
  return format(dateObj, 'yyyy년 MM월 dd일 EEEE', { locale: ko });
});

function submitDiary() {
  emit('submit', diaryContent.value);
}
</script>

<style scoped>
.diary-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 100%;
  width: 100%;
  margin: 0 auto;
  padding: 1rem 0; /* 여백 조정 */
}

.diary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.date {
  font-weight: bold;
  font-size: 1.2rem;
}

.user {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.profile-image {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.user-name {
  font-size: 1rem;
}

.label {
  margin-bottom: 0.5rem;
  font-size: 0.95rem;
}

.diary-textarea {
  width: 100%;
  display: block;
  min-height: 350px;
  padding: 1rem;
  border: 1px solid #ccc;
  border-radius: 8px;
  resize: vertical;
  margin-bottom: 0.8rem;
  font-size: 1rem;
  box-sizing: border-box;
  flex-grow: 1;
  overflow: auto;
}

.submit-button {
  background-color: #ffe9b5;
  border: none;
  padding: 0.7rem 1rem;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: 0.2s;
  width: 100%;
  box-sizing: border-box;
  font-size: 1rem;
  margin-top: 0.4rem;
  align-self: stretch;
}

.submit-button:hover {
  background-color: #ffd983;
}
</style>
