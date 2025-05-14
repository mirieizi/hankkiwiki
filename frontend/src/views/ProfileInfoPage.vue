<!-- src/views/ProfileInfoPage.vue -->
<template>
  <div class="profile-info-page">
    <h2>개인 정보</h2>
    <p class="greeting">{{ userNickname }}님, 안녕하세요!</p>

    <div class="button-group">
      <button @click="goToInfoEdit">개인 정보 수정</button>
      <button @click="goToHealthEdit">개인 건강 정보 수정</button>
    </div>
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
</script>

<style scoped>
.profile-info-page {
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
  background: darken(var(--orange-dark), 10%);
}
</style>
