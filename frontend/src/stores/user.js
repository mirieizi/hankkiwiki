// src/stores/user.js
import { defineStore } from "pinia";
import axios from "@/plugins/axios";

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
        const { data } = await axios.get("/user/profile");
        this.email = data.email;
        this.nickname = data.nickname;
      } catch {
        this.error = "프로필 정보를 불러오는 중 오류가 발생했습니다.";
      } finally {
        this.isLoading = false;
      }
    },
    // ...checkNickname, updateProfile 등은 그대로
    // **여기 추가!**
    async updateProfile(payload) {
      this.error = "";
      try {
        await axios.put("/user/profile", payload); // 백엔드에 따라 경로/메서드 확인!
        // 성공 시 스토어 데이터 최신화
        if (payload.nickname) this.nickname = payload.nickname;
        // 이메일은 바꾸지 않으니 그대로
        return true;
      } catch (e) {
        this.error = e.response?.data?.message || "프로필 수정에 실패했습니다.";
        return false;
      }
    },
  },
});
