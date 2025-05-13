<!-- src/components/registerFood/Diary.vue -->
<template>
  <div class="diary-container">
    <div class="diary-header">
      <p class="date">{{ formattedDate }}</p>
      <div class="user-info">
        <img src="@/assets/logo.png" alt="프로필" class="avatar" />
        <span>{{ userName }} 님</span>
      </div>
    </div>

    <p class="label">아래에 일기를 작성해주세요</p>

    <textarea v-model="diaryContent" class="diary-textarea" placeholder="오늘 하루는 어땠나요?"></textarea>

    <button class="submit-button" @click="submitDiary">등록하기</button>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { format } from "date-fns";
import { ko } from "date-fns/locale";

// props
const props = defineProps({
  date: {
    type: Date,
    required: true,
  },
  userName: {
    type: String,
    required: true,
  },
});

// emit
const emit = defineEmits(["submit"]);

const diaryContent = ref("");

const formattedDate = computed(() => format(props.date, "yyyy년 M월 d일 EEEE", { locale: ko }));

function submitDiary() {
  emit("submit", diaryContent.value);
}
</script>

<style scoped>
.diary-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 500px;
  margin: 0 auto;
}

.diary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.date {
  font-weight: bold;
  font-size: 1.2rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
}

.label {
  margin-bottom: 0.5rem;
}

.diary-textarea {
  width: 100%;
  height: 150px;
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid #ccc;
  resize: none;
  font-size: 1rem;
}

.submit-button {
  background-color: #ffe9b5;
  border: none;
  padding: 0.7rem 1.2rem;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: 0.2s;
}

.submit-button:hover {
  background-color: #ffd983;
}
</style>
