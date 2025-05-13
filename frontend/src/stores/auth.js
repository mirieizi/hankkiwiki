// src/stores/auth.js
import { defineStore } from "pinia";
import { ref } from "vue";

export const useAuthStore = defineStore("auth", () => {
  const isAuthenticated = ref(false);
  const user = ref(null);

  function login(userInfo) {
    isAuthenticated.value = true;
    user.value = userInfo;
  }

  function logout() {
    isAuthenticated.value = false;
    user.value = null;
  }

  return { isAuthenticated, user, login, logout };
});
