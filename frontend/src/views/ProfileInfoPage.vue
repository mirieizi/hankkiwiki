<!-- src/views/ProfileInfoPage.vue -->
<template>
  <div class="profile-info-page">
    <h2>개인 정보</h2>
    <p class="greeting">{{ userNickname }}님, 안녕하세요!</p>

    <div class="button-group">
      <button @click="goToInfoEdit">개인 정보 수정</button>
      <button @click="goToHealthEdit">개인 건강 정보 수정</button>
    </div>

    <!-- 탈퇴 버튼 (하단 우측) -->
    <button class="delete-btn" @click="confirmDelete">탈퇴하기</button>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();
const userNickname = ref("");

onMounted(async () => {
  try {
    const { data } = await axios.get("/api/user/profile");
    userNickname.value = data.nickname;
  } catch {
    userNickname.value = "사용자";
  }
});

function goToInfoEdit() {
  router.push({ name: "ProfileUser" });
}

function goToHealthEdit() {
  router.push({ name: "ProfileHealth" });
}

async function confirmDelete() {
  const confirmed = window.confirm("정말 탈퇴하시겠습니까?");
  if (!confirmed) return;

  try {
    await axios.delete("/api/user");
    alert("탈퇴가 완료되었습니다.");
    router.push({ name: "Login" });
  } catch (e) {
    alert(e.response?.data?.message || "탈퇴 중 문제가 발생했습니다.");
  }
}
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
  /* 다크 모드 없이 호버 색상 유지 */
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
