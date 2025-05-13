<template>
  <div class="register-container">
    <h1 class="title">오늘 뭐 먹음!</h1>
    <div class="layout">
      <!-- 왼쪽: 음식 검색 및 선택 영역 -->
      <div class="search-section">
        <h2>음식 검색</h2>
        <p>음식 검색영역</p>
        <!-- 여기에 검색 바, 결과 리스트 등 추가 예정 -->
      </div>

      <!-- 오른쪽: 일기 작성 영역 -->
      <div class="diary-section">
        <div class="diary-header">
          <span class="date">{{ formattedDate }}</span>
          <div class="user">
            <img src="@/assets/logo.png" class="avatar" alt="user" />
            <span class="user-name">양미이 님</span>
          </div>
        </div>

        <p class="label">아래에 일기를 작성해주세요</p>
        <textarea v-model="diaryContent" class="diary-textarea" placeholder="오늘 하루는 어땠나요?"></textarea>

        <button class="submit-button" @click="submitDiary">등록하기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { format } from "date-fns";
import { ko } from "date-fns/locale";

const today = new Date();
const formattedDate = computed(() => format(today, "yyyy년 M월 d일 EEEE", { locale: ko }));
const diaryContent = ref("");
function submitDiary() {
  console.log("일기 내용:", diaryContent.value);
}
</script>

<style scoped>
.register-container {
  padding: 2rem;
  max-width: 1600px;
  margin: auto;
}

.title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 2rem;
  text-align: center;
}

.layout {
  display: flex;
  flex-wrap: wrap;
  gap: 3rem;
  justify-content: center;
  align-items: stretch;
}

.search-section,
.diary-section {
  flex: 1 1 600px;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
  min-height: 600px;
}

.search-section {
  background-color: #f8f8f8;
}

.diary-section {
  background-color: #fff8e1;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  min-height: 800px;
  height: auto;
  position: relative;
  padding-bottom: 5rem;
  overflow: hidden; /* 버튼 잘림 방지 & 하단 끊기 */
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 8rem; /* footer와 여백 확보 */
  box-sizing: border-box;
  isolation: isolate;
  z-index: 1;
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

.avatar {
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
  min-height: 350px;
  padding: 1rem;
  border: 1px solid #ccc;
  border-radius: 8px;
  resize: vertical;
  margin-bottom: 1.2rem;
  font-size: 1rem;
  box-sizing: border-box;
  flex-grow: 1;
  overflow: auto;
}

.submit-button {
  background-color: #ffe9b5;
  border: none;
  padding: 0.9rem 1.5rem;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  transition: 0.2s;
  width: 100%;
  box-sizing: border-box;
  font-size: 1rem;
  margin-top: 1.5rem;
  align-self: stretch;
}

.submit-button:hover {
  background-color: #ffd983;
}

@media (max-width: 768px) {
  .search-section,
  .diary-section {
    flex: 1 1 100%;
  }
  .layout {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
