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

    <textarea v-model="diaryContent" class="diary-textarea" placeholder="오늘 하루는 어땠나요?"></textarea>

    <button class="submit-button" @click="submitDiary" :disabled="loading">
      {{ loading ? "등록 중..." : "등록하기" }}
    </button>
  </div>
</template>

<script setup>
// Vue & router
import { ref, computed } from "vue";
import { useRouter } from "vue-router";

// 날짜 포맷터
import { format, parseISO } from "date-fns";
import { ko } from "date-fns/locale";

// HTTP 클라이언트
import axios from "axios";
axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";
axios.defaults.withCredentials = true;

// props
const props = defineProps({
  date: { type: String, required: true },
  userName: { type: String, required: true },
});

// 내부 상태
const diaryContent = ref("");
const loading = ref(false);
const router = useRouter();

// 화면에 표시할 포맷된 날짜
const formattedDate = computed(() => {
  const dateObj = parseISO(props.date);
  return format(dateObj, "yyyy년 MM월 dd일 EEEE", { locale: ko });
});

// 다이어리 등록
async function submitDiary() {
  if (!diaryContent.value.trim()) {
    alert("일기 내용을 입력해주세요.");
    return;
  }

  loading.value = true;
  try {
    // POST /api/user/me/diaries
    await axios.post("/api/user/me/diaries", {
      date: props.date,
      content: diaryContent.value,
    });
    alert("등록이 완료되었습니다!");
    // 등록 후 달력 페이지 등으로 이동
    router.push("/calendar");
  } catch (e) {
    if (e.response?.status === 401) {
      alert("로그인이 필요합니다.");
      router.push("/login");
    } else {
      console.error("Diary 등록 실패:", e);
      alert("등록 중 오류가 발생했습니다.");
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
  gap: 1rem;
  max-width: 600px;
  width: 100%;
  margin: 0 auto;
  padding: 1rem 0;
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

.label {
  margin-bottom: 0.5rem;
  font-size: 0.95rem;
}

.diary-textarea {
  width: 100%;
  min-height: 350px;
  padding: 1rem;
  border: 1px solid #ccc;
  border-radius: 8px;
  resize: vertical;
  font-size: 1rem;
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
  font-size: 1rem;
}

.submit-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.submit-button:hover:enabled {
  background-color: #ffd983;
}
</style>
