<template>
  <aside class="app-sidebar">
    <!-- 사이드바 로고 및 타이틀 -->
    <div class="sidebar-header"></div>

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
          <RouterLink to="/food/register" class="sidebar-link">오늘 뭐 먹음!</RouterLink>
        </li>
        <li>
          <RouterLink to="/calendar" class="sidebar-link">캘린더</RouterLink>
        </li>
        <li><RouterLink :to="{ name: 'ProfileInfo' }" class="sidebar-link">마이페이지</RouterLink></li>
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
  background: linear-gradient(180deg, #f6faf8 70%, #fff 100%);
  border-right: none;
  padding: 2rem 1.1rem 1.5rem 1.1rem;
  min-width: 180px;
  position: sticky;
  top: var(--header-height);
  height: calc(100vh - var(--header-height));
  box-shadow: 12px 0 32px #21d59b09;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  font-family: "EFDiary", "Pretendard", "Noto Sans KR", sans-serif !important;
}

.sidebar-header {
  text-align: center;
  margin-bottom: 1.4rem;
}
.sidebar-header img {
  width: 90px;
  margin-bottom: 0.3rem;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 1.5px 9px #21d59b17;
  padding: 4px 0;
}
.sidebar-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: #1bb67a;
  margin: 0;
  letter-spacing: 0.5px;
  opacity: 0.95;
}

.sidebar-nav {
  width: 100%;
}
.sidebar-nav > ul {
  display: flex;
  flex-direction: column;
  gap: 1.1rem;
  padding: 0.7rem 0.6rem;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 2px 14px #21d59b0c;
}

.sidebar-nav > ul > li {
  font-weight: 700;
  font-size: 1.09rem;
  color: #16ac72;
  margin-bottom: 0.05rem;
  position: relative;
}
.sidebar-nav > ul > li:not(:last-child) {
  padding-bottom: 0.6rem;
  border-bottom: 1px dashed #e7f2ec;
}

.sidebar-nav li ul {
  margin: 0.18rem 0 0.1rem 0.5rem;
  padding-left: 0.8rem;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 0.13rem;
}
.sidebar-nav li ul li {
  font-size: 0.97rem;
  font-weight: 500;
  color: #2f8e6e;
  position: relative;
  padding-left: 0.7em;
}
.sidebar-nav li ul li::before {
  content: "•";
  position: absolute;
  left: 0;
  color: #ffc83d;
  font-size: 1.05em;
  top: 0.09em;
}
.sidebar-link {
  display: block;
  padding: 0.25rem 0.7rem;
  border-radius: 9px;
  color: #14a17e;
  background: none;
  text-decoration: none;
  font-size: 1.03rem;
  font-weight: 500;
  margin-bottom: 1.5px;
  transition: background 0.13s, color 0.14s;
}
.sidebar-link.router-link-active {
  background: #f8faee;
  color: #ffc83d;
  font-weight: 700;
  border-left: 4px solid #ffc83d;
}
.sidebar-link:hover {
  background: #eafbf3;
  color: #19b58b;
  text-decoration: underline;
}

/* 더 정보성 위키 느낌 아이콘, 구분선, 색조정 */
.sidebar-nav > ul > li::before {
  content: "📚";
  margin-right: 0.35em;
  font-size: 1.03em;
  opacity: 0.74;
  vertical-align: -0.1em;
}
.sidebar-nav > ul > li:first-child::before {
  content: "🍚";
}
.sidebar-nav > ul > li:nth-child(2)::before {
  content: "📝";
}
.sidebar-nav > ul > li:nth-child(3)::before {
  content: "📅";
}
.sidebar-nav > ul > li:last-child::before {
  content: "👤";
}

/* 모바일 */
@media (max-width: 700px) {
  .app-sidebar {
    width: 100vw;
    padding: 1rem 0.4rem 0.6rem 0.4rem;
    min-width: 0;
  }
  .sidebar-header img {
    width: 60px;
  }
  .sidebar-nav > ul {
    border-radius: 14px;
    padding: 0.2rem 0.2rem;
  }
}
</style>
