// src/main.js

import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import { createPinia } from "pinia";
import "@/assets/global.css";
import axios from "@/plugins/axios";
import { useAuthStore } from "@/stores/auth";
import { useKakao } from "vue3-kakao-maps/@utils";
import Vue3Toastify, { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";

// 1. Axios 기본 설정
console.log("🎯 API Base URL:", axios.defaults.baseURL);

axios.interceptors.request.use((req) => {
  console.log(`➡️ [Axios] ${req.method.toUpperCase()} →`, req.baseURL + req.url);
  return req;
});

// 2. 앱 생성 및 플러그인 등록
const app = createApp(App);
const pinia = createPinia();
app.use(pinia);
app.use(router); // 반드시 router 먼저 use!

// ✅ 토스트 플러그인 등록
app.use(Vue3Toastify, {
  autoClose: 3000,
  position: "top-right",
  hideProgressBar: false,
  closeOnClick: true,
  pauseOnHover: true,
  draggable: true,
  theme: "auto",
});

// 3. 외부 라이브러리 등록 (필요시)
useKakao(import.meta.env.VITE_KAKAO_API_KEY);
// console.log("카카오 API 키:", import.meta.env.VITE_KAKAO_API_KEY);

// 4. 인증스토어 초기화 후 마운트
const authStore = useAuthStore(pinia);
authStore.initialize().finally(() => {
  app.mount("#app"); // router, pinia 등록 후 mount!
});

// 5. Axios 토큰 기본값
const token = localStorage.getItem("accessToken");
if (token) {
  axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
}

// ✅ 전역 토스트 함수 내보내기 (선택사항)
export { toast };
