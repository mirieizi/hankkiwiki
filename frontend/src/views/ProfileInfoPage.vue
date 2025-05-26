<template>
  <div class="profile-info-wrapper">
    <!-- 로그인 필요 안내: 주석 해제하면 로그인 체크가 활성화됩니다 -->
    <!--
    <div v-if="!isLoggedIn" class="login-prompt">
      <p>로그인이 필요합니다.</p>
      <button @click="goLogin" class="btn-login">로그인하러 가기</button>
    </div>
    -->

    <div class="profile-info-card">
      <h2>내 정보</h2>
      <p class="greeting">{{ userNickname }}님, 안녕하세요!</p>

      <div class="button-group">
        <button class="btn-primary" @click="goToInfoEdit">개인 정보 수정</button>
        <button class="btn-primary" @click="goToHealthEdit">건강 정보 수정</button>
      </div>

      <button class="btn-delete" @click="confirmDelete">탈퇴하기</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "@/plugins/axios";

const router = useRouter();
const userNickname = ref("");
const isLoggedIn = ref(true);

function goLogin() {
  router.push("/login");
}
function goToInfoEdit() {
  router.push({ name: "ProfileUser" });
}
function goToHealthEdit() {
  router.push({ name: "ProfileHealth" });
}
async function confirmDelete() {
  const ok = window.confirm("정말 탈퇴하시겠습니까?");
  if (!ok) return;
  try {
    await axios.delete("/user/me");
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

onMounted(async () => {
  try {
    const { data } = await axios.get("/user/me");
    userNickname.value = data.nickname;
  } catch (e) {
    if (e.response?.status === 401) {
      isLoggedIn.value = false;
    } else {
      userNickname.value = "사용자";
    }
  }
});
</script>

<style scoped>
.profile-info-wrapper {
  background: #f7fafc;
  /* 부모 컨테이너 폭에 맞춰 꽉 채우도록 변경 */
  width: 100%;
  /* 높이는 자동, 부모 레이아웃 높이 따라감 */
  min-height: calc(100vh - 4rem);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 6rem;
  margin: 0 auto;
}

.login-prompt {
  background: #ffffff;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  text-align: center;
  margin-bottom: 2rem;
}

.btn-login {
  margin-top: 1rem;
  background-color: #21d59b;
  color: #ffffff;
  border: none;
  border-radius: 24px;
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
}
.btn-login:hover {
  background-color: #1aa28a;
}

.profile-info-card {
  background: #ffffff;
  width: 100%;
  max-width: 600px;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  text-align: center;
  position: relative;
}

h2 {
  font-size: 1.75rem;
  color: #333333;
  margin-bottom: 0.5rem;
}

.greeting {
  font-size: 1.125rem;
  color: #666666;
  margin-bottom: 0.5rem;
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.btn-primary {
  background-color: #21d59b;
  color: #ffffff;
  border: none;
  border-radius: 24px;
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
}
.btn-primary:hover {
  background-color: #1aa28a;
}

.btn-delete {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: none;
  color: #d32f2f;
  border: 1px solid #d32f2f;
  border-radius: 12px;
  padding: 0.4rem 0.8rem;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s;
}
.btn-delete:hover {
  background-color: #d32f2f;
  color: #ffffff;
}

@media (max-width: 600px) {
  .profile-info-wrapper {
    align-items: center;
    padding-top: 2rem;
  }
  .profile-info-card {
    padding: 1.5rem;
    margin-top: 1rem;
  }
  .button-group {
    flex-direction: column;
    gap: 0.75rem;
  }
  .btn-primary {
    width: 100%;
  }
}
</style>
