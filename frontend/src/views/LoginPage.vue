<!-- src/views/LoginPage.vue -->
<template>
  <div class="login-wrapper">
    <div :class="['login-container', { 'sign-up-mode': !signIn }]">
      <FormPanel :signIn="signIn" />
      <ActionPanel :signIn="signIn" @slide="toggleMode" />
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import FormPanel from "@/components/login/FormPanel.vue";
import ActionPanel from "@/components/login/ActionPanel.vue";

const signIn = ref(true);
function toggleMode() {
  signIn.value = !signIn.value;
}
</script>

<style>
@import "@/assets/global.css"; /* CSS 변수 로드 */

/* LoginPage.vue 끝에 추가 */
.login-container .action-panel {
  color: #000000 !important;
}

/* 전체 배경 + 중앙 배치 + 데코 도형 */
.login-wrapper {
  position: relative;
  width: 100%;
  height: 100vh;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
/* 원형 데코 (진한 오렌지) */
.login-wrapper::before {
  content: "";
  position: absolute;
  width: 350px;
  height: 350px;
  background: var(--orange-dark);
  border-radius: 50%;
  bottom: 50px;
  left: -150px;
}
/* 삼각형 데코 (진한 갈색) */
.login-wrapper::after {
  content: "";
  position: absolute;
  width: 300px;
  height: 300px;
  background: var(--brown-dark);
  clip-path: polygon(0 0, 100% 0, 100% 100%);
  top: -100px;
  right: -100px;
  transform: rotate(20deg);
}

/* 로그인/회원가입 컨테이너 */
.login-container {
  position: relative;
  width: 1000px;
  max-width: 90%;
  min-height: 600px;
  background: #fff;
  border-radius: 20px;
  display: flex;
  overflow: hidden;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.2);
}

/* 내부 패널 기본 스타일 */
.login-container .form-panel,
.login-container .action-panel {
  width: 50%;
  padding: 2rem;
  transition: transform 0.7s cubic-bezier(0.63, 0.39, 0.54, 0.91);
}

/* 로그인 패널 배경색 */
.login-container .form-panel {
  background-color: #faf1e6;
}

/* 회원가입 패널 배경색 및 텍스트색 */
.login-container .action-panel {
  background-color: #fdfaf6;
  color: var(--brown-dark);
}

/* 슬라이드 적용 */
.login-container.sign-up-mode .form-panel {
  transform: translateX(100%);
}
.login-container.sign-up-mode .action-panel {
  transform: translateX(-100%);
}

/* 버튼 컬러 예시 */
.login-container .form-panel button {
  background-color: var(--blue-info);
  color: aliceblue;
}
.login-container .action-panel button {
  background-color: var(--orange-dark);
  color: white;
}
</style>
