<!-- src/views/ProfileInfoPage.vue -->
<template>
  <div class="profile-info-page">
    <h2>개인 정보</h2>

    <!-- 로그인 필요 안내 -->
    <div v-if="!isLoggedIn" class="login-prompt">
      <p>로그인이 필요합니다.</p>
      <button @click="goLogin">로그인하러 가기</button>
    </div>

    <!-- 실제 프로필 정보 -->
    <div v-else>
      <p class="greeting">{{ userNickname }}님, 안녕하세요!</p>

      <div class="button-group">
        <button @click="goToInfoEdit">개인 정보 수정</button>
        <button @click="goToHealthEdit">개인 건강 정보 수정</button>
      </div>

      <!-- 탈퇴 버튼 (하단 우측) -->
      <button class="delete-btn" @click="confirmDelete">탈퇴하기</button>
    </div>
  </div>
</template>

<script setup>
// Composition API + 한국어 주석
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();

const userNickname = ref("");
const isLoggedIn = ref(true); // 로그인 상태 플래그

// 로그인 페이지로 이동
function goLogin() {
  router.push("/login");
}

// 개인정보 수정 페이지로 이동
function goToInfoEdit() {
  router.push({ name: "ProfileUser" });
}

// 건강정보 수정 페이지로 이동
function goToHealthEdit() {
  router.push({ name: "ProfileHealth" });
}

// 회원 탈퇴 처리
async function confirmDelete() {
  const ok = window.confirm("정말 탈퇴하시겠습니까?");
  if (!ok) return;

  try {
    await axios.delete("/api/user/me");
    alert("탈퇴가 완료되었습니다.");
    router.push({ name: "Login" });
  } catch (e) {
    if (e.response?.status === 401) {
      isLoggedIn.value = false;
    } else {
      alert(e.response?.data?.message || "탈퇴 중 문제가 발생했습니다.");
    }
  }
}

// 컴포넌트 마운트 시 내 프로필 정보 로드
onMounted(async () => {
  try {
    const { data } = await axios.get("/api/user/me");
    userNickname.value = data.nickname;
  } catch (e) {
    if (e.response?.status === 401) {
      // 인증 실패 시 로그인 안내
      isLoggedIn.value = false;
    } else {
      userNickname.value = "사용자";
    }
  }
});
</script>

<style scoped>
.profile-info-page {
  position: relative;
  max-width: 600px;
  margin: 2rem auto;
  padding: 2rem;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.login-prompt {
  padding: 2rem;
}

.login-prompt p {
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.login-prompt button {
  padding: 0.75rem 1.5rem;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

h2 {
  margin-bottom: 1rem;
}

.greeting {
  margin-bottom: 2rem;
  font-size: 1.25rem;
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.button-group button {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  background: var(--orange-dark);
  color: #fff;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
}

.button-group button:hover {
  opacity: 0.9;
}

.delete-btn {
  position: absolute;
  bottom: 0.75rem;
  right: 0.75rem;
  padding: 0.25rem 0.5rem;
  font-size: 0.75rem;
  color: #fff;
  background: #d32f2f;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.delete-btn:hover {
  opacity: 1;
}
</style>
