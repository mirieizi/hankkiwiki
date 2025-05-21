<template>
  <header class="app-header">
    <div class="header-content">
      <RouterLink to="/" class="logo">
        <img src="@/assets/logo.png" alt="한끼위키 로고" />
      </RouterLink>

      <nav class="nav-links">
        <!-- 로그인 안 한 상태 -->
        <template v-if="!isAuthenticated">
          <RouterLink to="/login" class="nav-link">로그인</RouterLink>
          <RouterLink to="/signup" class="nav-link">회원가입</RouterLink>
        </template>

        <!-- 로그인 한 상태 -->
        <template v-else>
          <RouterLink to="/profile/info" class="nav-link">마이페이지</RouterLink>
          <a href="#" @click.prevent="logout" class="nav-link">로그아웃</a>
        </template>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { computed } from "vue";
import { useAuthStore } from "@/stores/auth";
import { RouterLink, useRouter } from "vue-router";

const authStore = useAuthStore();
const isAuthenticated = computed(() => authStore.isAuthenticated);
const router = useRouter();
const logout = () => {
  authStore.logout();
  router.push("/");
};
</script>

<style scoped>
.app-header {
  background: #ffdba4;
  padding: 0.8rem 2rem;
}

.header-content {
  display: flex;
  align-items: center;
  /* 로고와 네비를 좌우로 붙이기 위해 justify-content 제거 */
}

.logo {
  /* 로고는 왼쪽에 고정 */
  margin-right: auto;
}

.logo img {
  height: 50px;
}

.nav-links {
  /* 네비 링크들은 오른쪽에 고정 */
  display: flex;
  align-items: center;
  margin-left: auto;
}

.nav-link {
  margin-left: 2rem;
  color: #333;
  text-decoration: none;
  font-size: 1.2rem;
}

.nav-link:hover {
  text-decoration: underline;
}
</style>
