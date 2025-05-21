// src/stores/auth.js

import { defineStore } from "pinia";
import axios from "@/plugins/axios";
import router from "@/router";
import { useUserStore } from "@/stores/user";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    isAuthenticated: false,
    userInfo: null,
    loading: false,
    error: "",
    redirectPath: null,
    emailDupError: "",
    emailChecked: false,
    isCheckignEmailDup: false,
  }),

  actions: {
    // ① 로그인
    async login(credentials) {
      this.loading = true;
      this.error = "";
      try {
        // 로그인 요청
        const { data } = await axios.post("/auth/login", credentials);

        // 1. 토큰 저장
        localStorage.setItem("accessToken", data.accessToken);
        localStorage.setItem("refreshToken", data.refreshToken);

        // 2. 프로필 불러오기 (토큰 반영된 상태로!)
        const userStore = useUserStore();
        console.log("fetchProfile() 호출 전: ", userStore.userInfo);
        await userStore.fetchProfile();
        console.log("fetchProfile() 호출 후:", userStore.userInfo);
        this.userInfo = userStore.userInfo;
        this.isAuthenticated = true;

        // 3. 리다이렉트
        router.push(this.redirectPath || "/");
      } catch (e) {
        console.log("로그인 실패", e);
        this.error = e.response?.data?.message || e.message || "로그인에 실패했습니다.";
        this.isAuthenticated = false;
        this.userInfo = null;
      } finally {
        this.loading = false;
      }
    },

    // ② 로그아웃
    async logout() {
      try {
        await axios.delete("/auth/logout");
      } catch (e) {
        console.warn("서버 로그아웃 실패:", e);
      } finally {
        // 토큰 삭제
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        this.isAuthenticated = false;
        this.userInfo = null;

        // User 스토어 초기화
        const userStore = useUserStore();
        userStore.$reset();

        // 로그인 페이지로 이동
        router.push({ name: "Login" });
      }
    },

    // ③ 앱 시작 시 토큰 체크 및 프로필 로드
    async initialize() {
      const token = localStorage.getItem("accessToken");
      if (token) {
        try {
          // 토큰이 있으면 프로필 불러오기
          const userStore = useUserStore();
          await userStore.fetchProfile();
          this.userInfo = userStore.userInfo;
          this.isAuthenticated = true;
        } catch (e) {
          // 토큰 만료 등 에러 시 로그아웃
          this.logout();
        }
      } else {
        this.isAuthenticated = false;
        this.userInfo = null;
      }
    },

    // ④ 토큰 수동 갱신
    async refreshToken() {
      try {
        const refreshToken = localStorage.getItem("refreshToken");
        if (!refreshToken) throw new Error("리프레시 토큰이 없습니다.");
        const { data } = await axios.post("/auth/refresh", { refreshToken });
        localStorage.setItem("accessToken", data.accessToken);
        this.isAuthenticated = true;
        // 프로필 다시 불러오기
        const userStore = useUserStore();
        await userStore.fetchProfile();
        this.userInfo = userStore.userInfo;
      } catch (e) {
        this.logout();
      }
    },

    async checkEmailDup(email) {
      this.emailDupError = "";
      this.emailChecked = false;
      const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRe.test(email)) {
        this.emailDupError = "유효한 이메일 주소가 아닙니다.";
        return;
      }
      this.isCheckingEmailDup = true;
      try {
        const res = await axios.get("/auth/check-email", {
          params: { email },
        });
        if (res.data.available) {
          this.emailChecked = true;
        } else {
          this.emailDupError = "이미 사용 중인 이메일입니다.";
        }
      } catch {
        this.emailDupError = "이메일 확인에 실패했습니다.";
      } finally {
        this.isCheckingEmailDup = false;
      }
    },

    // ⑤ 로그인 직전 리다이렉트 경로 설정
    setRedirectPath(path) {
      this.redirectPath = path;
    },
    actions: {
      async checkEmailDup(email) {
        this.emailDupError = "";
        this.emailChecked = false;
        const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRe.test(email)) {
          this.emailDupError = "유효한 이메일 주소가 아닙니다.";
          return;
        }
        this.isCheckingEmailDup = true;
        try {
          const res = await axios.get("/auth/check-email", {
            params: { email },
          });
          if (res.data.available) {
            this.emailChecked = true;
          } else {
            this.emailDupError = "이미 사용 중인 이메일입니다.";
          }
        } catch {
          this.emailDupError = "이메일 확인에 실패했습니다.";
        } finally {
          this.isCheckingEmailDup = false;
        }
      },
    },
  },
});
