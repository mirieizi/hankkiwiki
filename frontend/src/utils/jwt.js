// JWT 토큰 디코딩 유틸리티
export function decodeJWT(token) {
    try {
      if (!token) return null;
      
      // JWT는 header.payload.signature 형태
      const parts = token.split('.');
      if (parts.length !== 3) return null;
      
      // payload 부분 디코딩
      const payload = parts[1];
      
      // Base64URL 디코딩을 위한 패딩 추가
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const paddedBase64 = base64.padEnd(base64.length + (4 - base64.length % 4) % 4, '=');
      
      // 디코딩 및 JSON 파싱
      const decodedPayload = atob(paddedBase64);
      return JSON.parse(decodedPayload);
    } catch (error) {
      console.error('JWT 디코딩 실패:', error);
      return null;
    }
  }
  
  // 사용자 정보 추출
  export function getUserFromToken(token = null) {
    const targetToken = token || localStorage.getItem('accessToken');
    if (!targetToken) return null;
    
    const payload = decodeJWT(targetToken);
    if (!payload) return null;
    
    return {
      userId: payload.userId || payload.id || payload.sub,
      username: payload.username || payload.name || payload.userName,
      email: payload.email,
      roles: payload.roles || [],
      // 기타 필요한 필드들
    };
  }
  
  // 토큰 유효성 검사
  export function isTokenValid(token = null) {
    const targetToken = token || localStorage.getItem('accessToken');
    if (!targetToken) return false;
    
    const payload = decodeJWT(targetToken);
    if (!payload) return false;
    
    // 만료 시간 확인
    if (payload.exp) {
      const currentTime = Math.floor(Date.now() / 1000);
      return payload.exp > currentTime;
    }
    
    return true;
  }
  