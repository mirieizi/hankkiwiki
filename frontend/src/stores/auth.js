import { defineStore } from "pinia";
import axios from "axios";
import router from "@/router";
import { useUserStore } from "@/stores/user";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    isAuthenticated: false,
    userInfo: null,
    loading: false,
    error: "",
  }),
  actions: {
    // ① 로그인
    async login(credentials) {
      this.loading = true;
      this.error = "";
      try {
        const { data } = await axios.post("/api/auth/login", credentials);
        // (예: data = { token, user } 형태)
        localStorage.setItem("token", data.token);
        axios.defaults.headers.common["Authorization"] = `Bearer ${data.token}`;
        this.isAuthenticated = true;
        this.userInfo = data.user;

        // 프로필 스토어도 초기화
        const userStore = useUserStore();
        await userStore.fetchProfile();
        router.push(this.redirectPath || "/");
      } catch (e) {
        this.error = e.response?.data?.message || "로그인에 실패했습니다.";
      } finally {
        this.loading = false;
      }
    },

    // ② 로그아웃
    logout() {
      localStorage.removeItem("token");
      delete axios.defaults.headers.common["Authorization"];
      this.isAuthenticated = false;
      this.userInfo = null;

      // User 스토어 초기화
      const userStore = useUserStore();
      userStore.$reset();

      router.push({ name: "Login" });
    },

    // ③ 앱 시작할 때 토큰 체크
    initialize() {
      const token = localStorage.getItem("token");
      if (token) {
        axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
        this.isAuthenticated = true;
        // 프로필 불러오기
        const userStore = useUserStore();
        return userStore.fetchProfile();
      }
    },
  },
});
