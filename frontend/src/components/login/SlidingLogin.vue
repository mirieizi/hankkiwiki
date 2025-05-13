<!-- src/components/SlidingLogin.vue -->
<template>
  <div :class="['container', { 'sign-up-mode': !signIn }]">
    <FormPanel :signIn="signIn" />
    <ActionPanel :signIn="signIn" @slide="toggleMode" />
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

<style scoped>
/* 전역 변수(컬러 팔레트) 가져오기 */
@import "@/assets/global.css";

/* 컨테이너 기본 */
.container {
  position: relative;
  width: 800px;
  max-width: 90vw;
  min-height: 500px;
  background: #fff;
  overflow: hidden;
  display: flex;
  border-radius: 20px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.2);
}

/* 로그인(form) 패널 */
.container .form-panel {
  background: var(--butter-bg);
  width: 50%;
  padding: 2rem;
  transition: transform 0.7s cubic-bezier(0.63, 0.39, 0.54, 0.91);
}

/* 회원가입(action) 패널 */
.container .action-panel {
  background: var(--orange-light);
  color: var(--brown-dark);
  width: 50%;
  padding: 2rem;
  transition: transform 0.7s cubic-bezier(0.63, 0.39, 0.54, 0.91);
}

/* 슬라이드 토글 */
.container.sign-up-mode .form-panel {
  transform: translateX(100%);
}
.container.sign-up-mode .action-panel {
  transform: translateX(-100%);
}

/* 버튼 등 주요 컬러 예시 */
.container .action-panel button {
  background-color: var(--orange-dark);
  color: white;
}
.container .form-panel button {
  background-color: var(--blue-info);
  color: white;
}
</style>
