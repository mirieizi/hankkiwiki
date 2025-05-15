// src/router/index.js
import { createRouter, createWebHistory } from "vue-router";

// Views
import Home from "@/views/Home.vue";
import About from "@/views/About.vue";
import LoginPage from "@/views/LoginPage.vue";
import ProfileInfoPage from "@/views/ProfileInfoPage.vue";
import HealthInfoPage from "@/views/HealthInfoPage.vue";
import UserInfoPage from "@/views/UserInfoPage.vue";
import RandomRecommend from "@/views/RandomRecommend.vue";
import HistoryRecommend from "@/views/historyRecommend.vue";
import AiRecommend from "@/views/AiRecommend.vue";
import RegisterFood from "@/views/RegisterFood.vue";
import Calendar from "@/views/Calendar.vue";

// Route definitions
const routes = [
  // Main
  { path: "/", name: "Home", component: Home },
  { path: "/about", name: "About", component: About },

  // Auth
  { path: "/login", name: "Login", component: LoginPage, props: { signIn: true } },
  { path: "/signup", name: "Signup", component: LoginPage, props: { signIn: false } },

  // Profile
  { path: "/profile/info", name: "ProfileInfo", component: ProfileInfoPage },
  { path: "/profile/health", name: "ProfileHealth", component: HealthInfoPage },
  { path: "/profile/user", name: "ProfileUser", component: UserInfoPage },

  // Recommendations
  { path: "/recommend/random", name: "RandomRecommend", component: RandomRecommend },
  { path: "/recommend/history", name: "HistoryRecommend", component: HistoryRecommend },
  { path: "/recommend/ai", name: "AiRecommend", component: AiRecommend },

  // Food & Calendar
  { path: "/food/register", name: "RegisterFood", component: RegisterFood },
  { path: "/calendar", name: "Calendar", component: Calendar },
];

export default createRouter({
  history: createWebHistory(), // HTML5 history mode
  routes,
});
