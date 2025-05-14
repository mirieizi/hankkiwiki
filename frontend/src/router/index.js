// src/router/index.js
import { createRouter, createWebHistory } from "vue-router";

import Home from "@/views/Home.vue";
import About from "@/views/About.vue";
import LoginPage from "@/views/LoginPage.vue";
import ProfileInfoPage from "@/views/ProfileInfoPage.vue";
import HealthInfoPage from "@/views/HealthInfoPage.vue";
import UserInfoPage from "@/views/UserInfoPage.vue";
import RandomRecommend from "@/views/RandomRecommend.vue";
import historyRecommend from "@/views/historyRecommend.vue";
import AiRecommend from "../views/AiRecommend.vue";

const routes = [
  {
    path: "/",
    name: "Home",
    component: Home,
  },
  {
    path: "/about",
    name: "About",
    component: About,
  },

  // 1) 로그인 페이지 (/login)
  {
    path: "/login",
    name: "Login",
    component: LoginPage,
    props: { signIn: true }, // 로그인 모드
  },

  // 2) 회원가입 페이지 (/signup)
  {
    path: "/signup",
    name: "Signup",
    component: LoginPage,
    props: { signIn: false }, // 회원가입 모드
  },
  {
    path: "/profile/info", // 개인정보 수정
    name: "ProfileInfo",
    component: ProfileInfoPage,
  },
  {
    path: "/profile/health", // 건강 정보 수정
    name: "ProfileHealth",
    component: HealthInfoPage,
  },

  {
    path: "/profile/user",
    name: "ProfileUser",
    component: UserInfoPage,
  },
  {
    path: "/recommend/random",
    name: "RandomRecommend",
    component: RandomRecommend,
  },
  {
    path: "/recommend/history",
    name: "historyRecommend",
    component: historyRecommend,
  },
  {
    path: "/recommend/ai",
    name: "AiRecommend",
    component: AiRecommend,
  },
];

const router = createRouter({
  history: createWebHistory(), // HTML5 history 모드
  routes,
});

export default router;
