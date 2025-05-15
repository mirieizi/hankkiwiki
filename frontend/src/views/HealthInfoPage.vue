<!-- src/views/HealthInfoPage.vue -->
<template>
  <div class="health-page">
    <h2>개인 건강 정보 수정</h2>
    <form v-if="isLoggedIn" @submit.prevent="onSubmit" class="health-form">
      <!-- 성별 -->
      <label>
        성별
        <select v-model="form.gender" required>
          <option disabled value="">선택하세요</option>
          <option value="FEMALE">여자</option>
          <option value="MALE">남자</option>
        </select>
      </label>

      <!-- 나이 -->
      <label>
        나이 (세)
        <input v-model.number="form.age" type="number" min="0" max="150" placeholder="예: 30" required />
      </label>

      <!-- 키 -->
      <label>
        키 (cm)
        <input v-model.number="form.height" type="number" min="50" max="300" placeholder="예: 170" required />
      </label>

      <!-- 몸무게 -->
      <label>
        몸무게 (kg)
        <input v-model.number="form.weight" type="number" min="1" max="500" placeholder="예: 60" required />
      </label>

      <!-- 활동 계수 -->
      <label>
        운동 정도
        <select v-model="form.activityFactor" required>
          <option disabled value="">선택하세요</option>
          <option value="SEDENTARY">거의 운동 없음 (주 0–1회)</option>
          <option value="LIGHT">가벼운 운동 (주 1–3회)</option>
          <option value="MODERATE">보통 운동 (주 3–5회)</option>
          <option value="ACTIVE">적극적 운동 (주 5–7회)</option>
          <option value="VERY_ACTIVE">고강도/격렬 운동</option>
        </select>
      </label>

      <!-- 에러 메시지 -->
      <p v-if="error" class="error">{{ error }}</p>

      <button type="submit">저장하기</button>
    </form>

    <!-- 로그인 필요 알림 -->
    <div v-else class="login-prompt">
      <p>로그인이 필요합니다.</p>
      <button @click="goLogin">로그인하러 가기</button>
    </div>
  </div>
</template>

<script setup>
// Composition API + 한국어 주석
import { reactive, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();

// 폼 데이터 초기화
const form = reactive({
  gender: "",
  age: null,
  height: null,
  weight: null,
  activityFactor: "",
});

const error = ref("");
const isExist = ref(false); // 기존 데이터 존재 여부
const isLoggedIn = ref(true); // 로그인 상태 플래그

// Axios 기본 설정
axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";
axios.defaults.withCredentials = true;

// 로그인 페이지로 이동
function goLogin() {
  router.push("/login");
}

// 페이지 로드 시
onMounted(async () => {
  try {
    const res = await axios.get("/api/user/me/health");
    // 성공적으로 불러왔으면
    if (res.data && Object.keys(res.data).length > 0) {
      Object.assign(form, res.data);
      isExist.value = true;
    } else {
      isExist.value = false;
    }
  } catch (e) {
    // 401 Unauthorized 이면 로그인 필요
    if (e.response?.status === 401) {
      isLoggedIn.value = false;
    } else {
      error.value = "건강 정보를 불러오는 중 오류가 발생했습니다.";
    }
  }
});

// 입력값 검증 함수
function validate() {
  error.value = "";
  if (!form.gender) {
    error.value = "성별을 선택해주세요.";
    return false;
  }
  if (form.age === null || form.age < 0 || form.age > 150) {
    error.value = "유효한 나이를 입력해주세요 (0–150세).";
    return false;
  }
  if (!form.height || form.height < 50 || form.height > 300) {
    error.value = "유효한 키를 입력해주세요 (50–300cm).";
    return false;
  }
  if (!form.weight || form.weight < 1 || form.weight > 500) {
    error.value = "유효한 몸무게를 입력해주세요 (1–500kg).";
    return false;
  }
  if (!form.activityFactor) {
    error.value = "운동 정도를 선택해주세요.";
    return false;
  }
  return true;
}

// 폼 제출 핸들러
async function onSubmit() {
  if (!validate()) return;

  try {
    if (isExist.value) {
      // 기존 데이터가 있으면 PUT
      await axios.put("/api/user/me/health", { ...form });
    } else {
      // 없으면 POST
      await axios.post("/api/user/me/health", { ...form });
    }
    // 저장 후 개인정보 수정 페이지로 이동
    router.push({ name: "ProfileInfo" });
  } catch (e) {
    if (e.response?.status === 401) {
      // 세션 만료 등으로 인증 실패 시
      isLoggedIn.value = false;
    } else {
      error.value = e.response?.data?.message || "저장 중 문제가 발생했습니다.";
    }
  }
}
</script>

<style scoped>
.health-page {
  max-width: 500px;
  margin: 2rem auto;
  padding: 2rem;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.health-page h2 {
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
  text-align: center;
}

.health-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.health-form label {
  display: flex;
  flex-direction: column;
  font-weight: 500;
}

.health-form input,
.health-form select {
  margin-top: 0.5rem;
  padding: 0.5rem 0.75rem;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1rem;
}

.health-form button {
  margin-top: 1rem;
  padding: 0.75rem;
  border: none;
  border-radius: 6px;
  background: var(--orange-dark);
  color: #fff;
  font-size: 1rem;
  cursor: pointer;
}

.error {
  color: #d32f2f;
  font-size: 0.9rem;
  text-align: center;
}

.login-prompt {
  text-align: center;
  padding: 2rem;
}

.login-prompt p {
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.login-prompt button {
  padding: 0.75rem 1.5rem;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
</style>
