<template>
  <div class="home-container">
    <!-- 메인 콘텐츠 -->
    <div class="mascot-box" @click="openModal">
      <div class="mascot-face">🍚</div>
    </div>
    <h1 class="title">한끼위키</h1>
    <p class="subtitle">
      오늘 뭐 먹을지 고민될 때,
      <span class="point">한끼위키</span>
      !
    </p>
    <div class="button-group">
      <button class="main-btn" @click="goRecommend">오늘의 추천받기</button>
      <button class="sub-btn" @click="goCalendar">식단 기록하기</button>
      <button class="info-btn" @click="openModal">🧑‍💻 한끼위키 기술 자랑</button>
    </div>
    <!-- 아래 "살짝 보이는" 카드 미리보기 -->
    <div class="tech-modal-peek" @click="openModal">
      <div class="modal-notch"></div>
      <span class="peek-title">
        <span class="point2">우리가 직접 구현한</span>
        <span class="point3">핵심 기술들</span>
        <span class="peek-arrow">▲</span>
      </span>
    </div>
    <!-- 실제 모달 카드 -->
    <transition name="slide-up">
      <div v-if="showModal" class="tech-modal-overlay" @click.self="closeModal">
        <div class="tech-modal-sheet">
          <!-- 닫기 버튼 -->
          <button class="modal-close-btn" @click="closeModal">✕</button>
          <div class="modal-notch"></div>
          <div class="modal-title">
            <span class="point2">우리가 직접 구현한 한끼위키의</span>
            <span class="point3">핵심 기술들</span>
          </div>
          <ul class="tech-list">
            <li>
              <span class="emoji">🦾</span>
              <span class="tech-title">RAG 기반 AI 추천</span>
              <span class="desc">
                음식 기록을
                <b>LLM+벡터DB</b>
                로 분석, 최근과 가장 멀거나 가까운 음식 추천!
              </span>
            </li>
            <li>
              <span class="emoji">⚡️</span>
              <span class="tech-title">KNN, Embedding, Vector Search</span>
              <span class="desc">
                칼로리/영양소/요리법 등 수치를
                <b>벡터화</b>
                해 진짜 취향·영양 균형까지 반영!
              </span>
            </li>
            <li>
              <span class="emoji">🔒</span>
              <span class="tech-title">JWT 인증 + Redis Refresh</span>
              <span class="desc">
                <b>JWT</b>
                로 로그인/회원가입,
                <b>Redis</b>
                로 리프레시 토큰 안전관리!
              </span>
            </li>
            <li>
              <span class="emoji">🚀</span>
              <span class="tech-title">초고속 DB 설계·캐싱</span>
              <span class="desc">
                DB 인덱싱/정규화,
                <b>Redis</b>
                캐싱까지, 수만개 음식 0.1초 추천!
              </span>
            </li>
            <li>
              <span class="emoji">📱</span>
              <span class="tech-title">Vue3 + Spring Boot API</span>
              <span class="desc">
                FE는
                <b>Vue3</b>
                로 부드러운 UX, BE는
                <b>Spring Boot</b>
                로 확장성까지!
              </span>
            </li>
            <li>
              <span class="emoji">🌱</span>
              <span class="tech-title">기획·개발·디자인 올인원</span>
              <span class="desc">
                <b>2명이 기획~AI 파이프라인까지 직접 개발</b>
                !
              </span>
            </li>
          </ul>
          <button class="modal-main-btn" @click="closeModal">한끼위키 시작하기</button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
const router = useRouter();

function goRecommend() {
  router.push("/recommend/random");
}
function goCalendar() {
  router.push("/calendar");
}

const showModal = ref(false);
function openModal() {
  showModal.value = true;
}
function closeModal() {
  showModal.value = false;
}
</script>

<style scoped>
/* 기존 홈 스타일 유지 + peek 추가 */
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #6faf9b 60%, #fff 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 16px 70px 16px; /* peek 영역 때문에 하단 패딩 약간 추가 */
  position: relative;
}
.mascot-box {
  /* ...이전과 동일 ... */
}
.mascot-face {
  font-size: 48px;
  line-height: 1;
}
.title {
  font-size: 2.4rem;
  font-weight: bold;
  color: #fff;
  margin-bottom: 8px;
}
.subtitle {
  font-size: 1.2rem;
  color: #fff;
  margin-bottom: 32px;
  text-align: center;
}
.point {
  background: #fff3;
  padding: 2px 8px;
  border-radius: 12px;
  color: #ffc83d;
  font-weight: bold;
}
.button-group {
  display: flex;
  gap: 16px;
}
.main-btn,
.sub-btn,
.info-btn {
  padding: 12px 32px;
  font-size: 1rem;
  border-radius: 20px;
  border: none;
  outline: none;
  box-shadow: 0 2px 12px #21d59b20;
  cursor: pointer;
  font-weight: 600;
  transition: 0.15s;
}
.main-btn {
  background: #fff;
  color: #21d59b;
}
.main-btn:hover {
  background: #ffc83d;
  color: #fff;
}
.sub-btn {
  background: #ffc83d;
  color: #fff;
}
.sub-btn:hover {
  background: #fff;
  color: #21d59b;
}
.info-btn {
  background: #fff;
  color: #16b594;
  border: 1.5px solid #e3e8f0;
  border-radius: 16px;
  padding: 10px 18px;
  font-size: 0.98rem;
  font-weight: 500;
  box-shadow: 0 2px 8px #2cc6aa13;
  margin-left: 8px;
}
.info-btn:hover {
  background: #21d59b;
  color: #fff;
}

/* ======================= */
/* peek(살짝 보이는) 영역  */
.tech-modal-peek {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 97;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: none;
  cursor: pointer;
  pointer-events: auto;
}
.tech-modal-peek .modal-notch {
  width: 58px;
  height: 7px;
  background: #ececec;
  border-radius: 7px;
  margin: 8px auto 0 auto;
  box-shadow: 0 2px 6px #19a18a0b;
}
.peek-title {
  background: #fff;
  box-shadow: 0 6px 18px #21d59b0d;
  border-radius: 26px 26px 0 0;
  color: #16b594;
  font-size: 1.12rem;
  font-weight: 700;
  margin-top: 3px;
  padding: 12px 38px 10px 38px;
  text-align: center;
  display: flex;
  align-items: center;
  gap: 6px;
  border-bottom: 1.5px solid #eef5f5;
  letter-spacing: 0.3px;
  position: relative;
}
.point2 {
  color: #17b1a2;
  font-weight: 800;
}
.point3 {
  color: #ffc83d;
  font-weight: 800;
  margin-left: 4px;
}
.peek-arrow {
  color: #c0cfd1;
  margin-left: 12px;
  font-size: 1.2rem;
  font-weight: 900;
}

/* ======================= */
/* 토스 모달 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.33s cubic-bezier(0.68, 1.3, 0.43, 0.92);
}
.slide-up-enter-from {
  transform: translateY(100%);
  opacity: 0;
}
.slide-up-enter-to {
  transform: translateY(0);
  opacity: 1;
}
.slide-up-leave-from {
  transform: translateY(0);
  opacity: 1;
}
.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}
.tech-modal-overlay {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(22, 36, 36, 0.18);
  z-index: 99;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.tech-modal-sheet {
  width: 98vw;
  max-width: 430px;
  background: #fff;
  border-radius: 32px 32px 0 0;
  box-shadow: 0 -2px 32px #0ad4a920;
  padding: 34px 28px 32px 28px;
  margin-bottom: 0;
  min-height: 390px;
  animation: slide-up 0.3s cubic-bezier(0.43, 1.3, 0.53, 0.97);
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}
.modal-close-btn {
  position: absolute;
  top: 19px;
  right: 22px;
  font-size: 1.42rem;
  background: none;
  border: none;
  color: #acb8c2;
  cursor: pointer;
  z-index: 2;
  padding: 4px 8px;
  transition: color 0.15s;
}
.modal-close-btn:hover {
  color: #ff7e4b;
}
.modal-notch {
  width: 56px;
  height: 7px;
  background: #ececec;
  border-radius: 7px;
  margin: 0 auto 18px auto;
}
.modal-title {
  font-size: 1.23rem;
  font-weight: 800;
  color: #17b1a2;
  margin-bottom: 18px;
  letter-spacing: 0.7px;
  text-align: center;
}
.point2 {
  color: #17b1a2;
  font-weight: 800;
}
.point3 {
  color: #ffc83d;
  font-weight: 800;
  margin-left: 4px;
}
.tech-list {
  width: 100%;
  margin: 0 0 18px 0;
  padding: 0;
  list-style: none;
}
.tech-list li {
  margin-bottom: 16px;
  font-size: 1.04rem;
  background: #f7fefc;
  border-radius: 15px;
  padding: 15px 16px 12px 14px;
  box-shadow: 0 2px 8px #22e2d514;
}
.tech-list .emoji {
  font-size: 1.21rem;
  margin-right: 7px;
}
.tech-list .tech-title {
  font-weight: 800;
  color: #15b099;
  margin-right: 6px;
}
.tech-list .desc {
  display: block;
  color: #7fa1b5;
  font-size: 0.98rem;
  margin-top: 3px;
  font-weight: 400;
  letter-spacing: 0;
}
.modal-main-btn {
  background: #21d59b;
  color: #fff;
  border: none;
  border-radius: 17px;
  font-size: 1.07rem;
  padding: 14px 0;
  font-weight: 700;
  width: 100%;
  margin-top: 14px;
  box-shadow: 0 2px 12px #21d59b20;
  transition: background 0.18s;
}
.modal-main-btn:hover {
  background: #ffc83d;
  color: #233048;
}

@media (max-width: 600px) {
  .title {
    font-size: 1.6rem;
  }
  .mascot-box {
    width: 66px;
    height: 66px;
  }
  .mascot-face {
    font-size: 36px;
  }
  .tech-modal-sheet {
    max-width: 100vw;
    border-radius: 26px 26px 0 0;
    padding: 23px 6vw 18px 6vw;
  }
  .tech-modal-peek .peek-title {
    font-size: 0.99rem;
    padding: 11px 14vw 10px 14vw;
  }
}
</style>
