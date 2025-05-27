// src/router/index.js
import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/stores/auth";

// Views
import Home from "@/views/Home.vue";
import About from "@/views/About.vue";
import LoginPage from "@/views/LoginPage.vue";
import ProfileInfoPage from "@/views/ProfileInfoPage.vue";
import HealthInfoPage from "@/views/HealthInfoPage.vue";
import UserInfoPage from "@/views/UserInfoPage.vue";
import RecommendView from "@/views/RecommendView.vue";
import RegisterFood from "@/views/RegisterFood.vue";
import Calendar from "@/views/Calendar.vue";
import KakaoMap from "@/views/KakaoMap.vue";

// Route definitions
const routes = [
  // Public routes
  { path: "/", name: "Home", component: Home },
  { path: "/about", name: "About", component: About },
  { path: "/login", name: "Login", component: LoginPage, props: { signIn: true } },
  { path: "/signup", name: "Signup", component: LoginPage, props: { signIn: false } },

  // Profile pages (require auth)
  // { path: "/profile/info", name: "ProfileInfo", component: ProfileInfoPage, meta: { requiresAuth: true } },
  // { path: "/profile/health", name: "ProfileHealth", component: HealthInfoPage, meta: { requiresAuth: true } },
  // { path: "/profile/user", name: "ProfileUser", component: UserInfoPage, meta: { requiresAuth: true } },

  { path: "/profile/info", name: "ProfileInfo", component: ProfileInfoPage, meta: { requiresAuth: true } },
  { path: "/profile/health", name: "ProfileHealth", component: HealthInfoPage, meta: { requiresAuth: true } },
  { path: "/profile/user", name: "ProfileUser", component: UserInfoPage, meta: { requiresAuth: true } },

  // Recommendation pages
  { path: "/recommend/:mode", name: "Recommend", component: RecommendView, props: true, meta: { requiresAuth: true } },
  { path: "/recommend", redirect: "/recommend/random" },

  // Food & Calendar (require auth)
  { path: "/food/register", name: "RegisterFood", component: RegisterFood, meta: { requiresAuth: true } },
  { path: "/calendar", name: "Calendar", component: Calendar, meta: { requiresAuth: true } },
  { path: "/kakao-map", name: "kakaomap", component: KakaoMap, meta: { requiresAuth: true } },
];

const router = createRouter({
  history: createWebHistory(), // HTML5 history mode
  routes,
});

// Global navigation guard
router.beforeEach(async (to, from, next) => {
  const auth = useAuthStore();

  // Ensure auth initialization only once
  if (!auth._initialized) {
    auth._initialized = true;
    await auth.initialize?.();
  }

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    next({ name: "Login", query: { redirect: to.fullPath } });
  } else {
    next();
  }
});

export default router;
