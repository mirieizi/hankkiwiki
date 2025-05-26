<template>
  <div class="login-bg">
    <div class="login-container" :class="{ 'sign-up-mode': !signIn }">
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
.login-bg {
  /* 부모 컨테이너 안에서 여유롭게 중앙 정렬하도록 전체 폭이 아닌 가용 영역 100% 사용 */
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(120deg, #ffffff 40%, #ffffff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 0;
}

.login-container {
  display: flex;
  /* 화면 가로의 80%만 차지, 최대 1000px */
  width: 80vw;
  max-width: 1000px;
  /* 세로 높이 확보 */
  min-height: 600px;
  max-height: none;
  margin-top: -200px; /* 위로 30px 이동 */
  border-radius: 28px;
  box-shadow: 0 15px 40px #20c59c13;
  overflow: hidden;
  background: #e7faf6;
  transform: translateY(-20px); /* 조금 더 위로 조정 */
}

.login-container .form-panel,
.login-container .action-panel {
  flex: 1 1 0;
  width: 50%;
  transition: transform 0.7s cubic-bezier(0.63, 0.39, 0.54, 0.91);
  padding: 6rem 2.1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.login-container.sign-up-mode .form-panel {
  transform: translateX(100%);
}
.login-container.sign-up-mode .action-panel {
  transform: translateX(-100%);
}

/* 반응형 조정 */
@media (max-width: 900px) {
  .login-container {
    flex-direction: column;
    width: 95vw;
    min-height: auto;
    border-radius: 14px;
  }
  .login-container .form-panel,
  .login-container .action-panel {
    flex: 1 1 100%;
    width: 100%;
    padding: 1.3rem 0.7rem;
    min-height: 280px;
    transform: none !important;
  }
  .login-container .action-panel {
    border-top: 1.5px solid #f1f0e8;
  }
}
</style>
