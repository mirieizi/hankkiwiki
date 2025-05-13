// src/router/index.js
import { createRouter, createWebHistory } from "vue-router";

import Home from "@/views/Home.vue";
import About from "@/views/About.vue";
import LoginPage from "@/views/LoginPage.vue";

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
];

const router = createRouter({
  history: createWebHistory(), // HTML5 history 모드
  routes,
});

export default router;
