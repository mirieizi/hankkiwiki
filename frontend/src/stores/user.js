import { defineStore } from 'pinia';
import { getUserFromToken, isTokenValid } from '@/utils/jwt';

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    isAuthenticated: false,
    loading: false,
  }),

  getters: {
    userName: (state) => state.user?.username || '사용자',
    userId: (state) => state.user?.userId,
    userEmail: (state) => state.user?.email,
  },

  actions: {
    // JWT 토큰에서 사용자 정보 로드
    loadUserFromToken() {
      this.loading = true;
      
      try {
        const token = localStorage.getItem('accessToken');
        
        if (!token || !isTokenValid(token)) {
          this.clearUser();
          return false;
        }
        
        const userInfo = getUserFromToken(token);
        
        if (userInfo) {
          this.user = userInfo;
          this.isAuthenticated = true;
          console.log('사용자 정보 로드됨:', userInfo);
          return true;
        } else {
          this.clearUser();
          return false;
        }
      } catch (error) {
        console.error('사용자 정보 로드 실패:', error);
        this.clearUser();
        return false;
      } finally {
        this.loading = false;
      }
    },

    // 사용자 정보 초기화
    clearUser() {
      this.user = null;
      this.isAuthenticated = false;
    },

    // 로그아웃
    logout() {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      this.clearUser();
    },

    // 토큰 갱신 후 사용자 정보 재로드
    refreshUserInfo() {
      return this.loadUserFromToken();
    },
  },
});
