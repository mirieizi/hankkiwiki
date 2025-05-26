<template>
  <header class="app-header">
    <div class="header-content">
      <RouterLink to="/" class="logo">
        <img src="@/assets/logo3.png" alt="한끼위키 로고" />
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
  background: linear-gradient(90deg, #f6f8f5 70%, #fffbe8 100%);
  box-shadow: 0 2px 16px #ffc83d13;
  padding: 0.8rem 0;
  width: 100%;
  /* border-bottom: 1.5px solid #ffe5b6; */
}

.header-content {
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 0 2rem;
  max-width: 2400px;
}

.logo {
  margin-right: auto;
  display: flex;
  align-items: center;
}
.logo img {
  height: 88px;
  margin-left: 1px;
}

.nav-links {
  display: flex;
  align-items: center;
  margin-left: auto;
  gap: 1rem;
  margin-left: 0;
}

/* 기본 네비 버튼 */
.nav-link {
  margin-left: 1rem;
  color: #21d59b;
  text-decoration: none;
  font-size: 1.05rem;
  font-weight: 600;
  padding: 0.42rem 1.25rem;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 1px 4px #21d59b12;
  transition: background 0.18s, color 0.17s, box-shadow 0.13s;
  border: none;
  outline: none;
  display: inline-block;
  letter-spacing: -0.5px;
}

.nav-link:hover {
  background: #21d59b18;
  color: #18ad7c;
  box-shadow: 0 2px 8px #21d59b19;
  text-decoration: none;
}

/* 로그인/회원가입만 강조색 다르게 */
.nav-link[to="/login"],
.nav-link[to="/signup"] {
  color: #fff;
  background: #21d59b;
  box-shadow: 0 2px 12px #21d59b22;
}
.nav-link[to="/login"]:hover,
.nav-link[to="/signup"]:hover {
  background: #18ad7c;
  color: #fff;
}
</style>
