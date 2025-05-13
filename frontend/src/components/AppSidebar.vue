<template>
  <aside class="app-sidebar">
    <!-- 사이드바 로고 및 타이틀 -->
    <div class="sidebar-header">
      <RouterLink to="/">
        <img src="@/assets/logo.png" alt="한끼위키 로고" />
      </RouterLink>
      <h2 class="sidebar-title">한끼위키</h2>
    </div>

    <!-- 사이드바 메뉴 -->
    <nav class="sidebar-nav">
      <ul>
        <li>
          오늘 뭐 먹지?
          <ul>
            <li><RouterLink to="/recommend/random" class="sidebar-link">랜덤 추천</RouterLink></li>
            <li><RouterLink to="/recommend/history" class="sidebar-link">최근에 먹은 거</RouterLink></li>
            <li><RouterLink to="/recommend/ai" class="sidebar-link">AI 맛추 추천</RouterLink></li>
          </ul>
        </li>
        <li>
          <RouterLink to="/calendar" class="sidebar-link">캘린더</RouterLink>
        </li>
        <!-- 마이페이지 및 로그아웃 메뉴 -->
        <li>
          <RouterLink to="/mypage" class="sidebar-link">마이페이지</RouterLink>
        </li>
      </ul>
    </nav>
  </aside>
</template>

<script setup>
import { useAuthStore } from "@/stores/auth";
import { RouterLink, useRouter } from "vue-router";

const authStore = useAuthStore();
const router = useRouter();
const logout = () => {
  authStore.logout();
  router.push("/");
};
</script>

<style scoped>
.app-sidebar {
  width: 240px;
  background: #fff;
  border-right: 1px solid #e0e0e0;
  padding: 1rem;
  height: 100vh;
  position: sticky;
  top: var(--header-height); /* BaseLayout.vue 에서 정의한 --header-height */
  height: calc(100vh - var(--header-height));
}
.sidebar-header {
  text-align: center;
  margin-bottom: 2rem;
}
.sidebar-header img {
  width: 150px;
  margin-top: 0.5rem;
  margin-bottom: 0.5rem;
}
.sidebar-title {
  font-size: 1.25rem;
  font-weight: bold;
  margin: 0;
}
.sidebar-nav ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.sidebar-nav > ul > li {
  margin-bottom: 1rem;
  font-weight: 500;
}
.sidebar-nav li ul {
  list-style: disc;
  padding-left: 1.25rem;
  margin-top: 0.5rem;
}
.sidebar-link {
  display: block;
  padding: 0.25rem 0;
  color: #333;
  text-decoration: none;
  font-size: 1rem;
}
.sidebar-link.router-link-active {
  color: #000;
  font-weight: 600;
}
.sidebar-link:hover {
  text-decoration: underline;
}
</style>
