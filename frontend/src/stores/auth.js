import { defineStore } from "pinia";
import axios from "@/plugins/axios";
import router from "@/router";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    isAuthenticated: false,
    userInfo: null,
    loading: false,
    error: "",
    redirectPath: null,
    emailDupError: "",
    emailChecked: false,
    isCheckingEmail:false,
    nicknameDupError: "",
    nicknameChecked: false,
    isCheckingNickname: false
  }),
  actions: {
    // 로그인
    async login(credentials) {
      this.loading = true;
      this.error = "";
      try {
        const { data } = await axios.post("/auth/login", credentials);
        localStorage.setItem("accessToken", data.accessToken);
        localStorage.setItem("refreshToken", data.refreshToken);
        await this.fetchProfile();
        this.isAuthenticated = true;
        router.push(this.redirectPath || "/");
      } catch (e) {
        this.error = e.response?.data?.message || "로그인에 실패했습니다.";
        this.isAuthenticated = false;
        this.userInfo = null;
      } finally {
        this.loading = false;
      }
    },
    // 로그아웃
    async logout() {
      try {
        await axios.delete("/auth/logout");
      } catch {}
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      this.isAuthenticated = false;
      this.userInfo = null;
      router.push({ name: "Login" });
    },
    // 프로필 로딩
    async fetchProfile() {
      try {
        const { data } = await axios.get("/user/me");
        this.userInfo = data;
        this.isAuthenticated = true;
      } catch (e) {
        this.userInfo = null;
        this.isAuthenticated = false;
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        // 필요시, router.push("/login"); 여기서 해도 됨
      }
    },
    // 프로필 수정
    async updateProfile(payload) {
      try {
        await axios.patch("/user/me/health", payload);
        await this.fetchProfile();
        this.error = "";
        return true;
      } catch (e) {
        this.error = e.response?.data?.message || "정보 수정에 실패했습니다.";
        return false;
      }
    },
    // 닉네임 중복 확인
    async checkNickname(nickname) {
      this.nicknameDupError = "";
      this.nicknameChecked = false;
      this.isCheckingNickname = true;
      if (!nickname) return;
      const nickRe = /^[가-힣A-Za-z0-9]{1,10}$/;
      if (!nickRe.test(nickname)) {
        this.nicknameDupError = "닉네임은 최대10자, 공백·특수문자 없이 입력!";
        this.isCheckingNickname = false;
        return;
      }
      try {
        const res = await axios.get("/auth/check-nickname", { params: { nickname } });
        if (res.data.available) this.nicknameChecked = true;
        else this.nicknameDupError = "이미 사용 중인 닉네임입니다.";
      } catch {
        this.nicknameDupError = "닉네임 확인에 실패!";
      } finally {
        this.isCheckingNickname = false;
      }
    },
    // 이메일 중복 확인
    async checkEmailDup(email) {
      this.emailDupError = "";
      this.emailChecked = false;
      this.isCheckingEmail = true;

      const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRe.test(email)) {
        this.emailDupError = "유효한 이메일 주소가 아닙니다.";
        this.isCheckingEmail = false;
        return;
      }

      try {
        const res = await axios.get("/auth/check-email", { params: { email } });
        if (res.data.available) this.emailChecked = true;
        else this.emailDupError = "이미 사용 중인 이메일입니다.";
      } catch {
        this.emailDupError = "이메일 확인에 실패!";
      } finally {
        this.isCheckingEmail = false;
      }
    },
    async initialize() {
      const token = localStorage.getItem("accessToken");
      if (token) {
        try {
          await this.fetchProfile();
          this.isAuthenticated = true;
        } catch (e) {
          await this.logout();
        }
      } else {
        this.isAuthenticated = false;
        this.userInfo = null;
      }
    },
    // 리다이렉트 경로 저장
    setRedirectPath(path) {
      this.redirectPath = path;
    },
  },
});
