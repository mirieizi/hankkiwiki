<!-- src/views/HealthInfoPage.vue -->
<template>
  <div class="health-page">
    <h2>개인 건강 정보 수정</h2>
    <form @submit.prevent="onSubmit" class="health-form">
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
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();
const form = reactive({
  gender: "",
  age: null,
  height: null,
  weight: null,
  activityFactor: "",
});
const error = ref("");
const isExist = ref(false); // 데이터 존재 여부 체크

// 초기 데이터 로드
onMounted(async () => {
  try {
    const { data } = await axios.get("/api/user/health");
    if (data && Object.keys(data).length > 0) {
      Object.assign(form, data);
      isExist.value = true;
    } else {
      isExist.value = false;
    }
  } catch {
    error.value = "건강 정보를 불러오는 중 오류가 발생했습니다.";
    isExist.value = false;
  }
});

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

async function onSubmit() {
  if (!validate()) return;

  try {
    if (isExist.value) {
      await axios.put("/api/user/health", { ...form });
    } else {
      await axios.post("/api/user/health", { ...form });
    }
    router.push({ name: "ProfileInfo" });
  } catch (e) {
    error.value = e.response?.data?.message || "저장 중 문제가 발생했습니다.";
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
</style>
