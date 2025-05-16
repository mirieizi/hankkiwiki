// src/stores/user.js
import { defineStore } from "pinia";
import axios from "axios";

export const useUserStore = defineStore("user", {
  state: () => ({
    email: "",
    nickname: "",
    dupError: "",
    error: "",
    isLoading: false,
  }),
  actions: {
    async fetchProfile() {
      this.isLoading = true;
      this.error = "";
      try {
        const { data } = await axios.get("/api/user/profile");
        this.email = data.email;
        this.nickname = data.nickname;
      } catch {
        this.error = "프로필 정보를 불러오는 중 오류가 발생했습니다.";
      } finally {
        this.isLoading = false;
      }
    },
    // ...checkNickname, updateProfile 등은 그대로
  },
});
