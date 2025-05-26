<template>
  <div class="health-wrapper">
    <div class="health-card">
      <h2>개인 건강 정보 수정</h2>

      <form @submit.prevent="onSubmit" class="health-form">
        <!-- 성별 -->
        <div class="form-group">
          <label class="form-label">성별</label>
          <select v-model="form.gender" required class="form-select">
            <option disabled value="">선택하세요</option>
            <option value="FEMALE">여자</option>
            <option value="MALE">남자</option>
          </select>
        </div>
        <!-- 나이 -->
        <div class="form-group">
          <label class="form-label">나이 (세)</label>
          <input v-model.number="form.age" type="number" min="0" max="150" placeholder="예: 30" required class="form-input" />
        </div>
        <!-- 키 -->
        <div class="form-group">
          <label class="form-label">키 (cm)</label>
          <input v-model.number="form.height" type="number" min="50" max="300" placeholder="예: 170" required class="form-input" />
        </div>
        <!-- 몸무게 -->
        <div class="form-group">
          <label class="form-label">몸무게 (kg)</label>
          <input v-model.number="form.weight" type="number" min="1" max="500" placeholder="예: 60" required class="form-input" />
        </div>
        <!-- 활동 계수 -->
        <div class="form-group">
          <label class="form-label">운동 정도</label>
          <select v-model="form.activityFactor" required class="form-select">
            <option disabled value="">선택하세요</option>
            <option value="SEDENTARY">거의 운동 없음 (주 0–1회)</option>
            <option value="LIGHT">가벼운 운동 (주 1–3회)</option>
            <option value="MODERATE">보통 운동 (주 3–5회)</option>
            <option value="ACTIVE">적극적 운동 (주 5–7회)</option>
            <option value="VERY_ACTIVE">고강도/격렬 운동</option>
          </select>
        </div>

        <p v-if="error" class="feedback error">{{ error }}</p>
        <button type="submit" class="btn-primary">저장하기</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "@/plugins/axios";
const router = useRouter();

const form = reactive({
  gender: "",
  age: null,
  height: null,
  weight: null,
  activityFactor: "",
});
const error = ref("");
const isExist = ref(false);

onMounted(async () => {
  try {
    const res = await axios.get("/user/me/health");
    if (res.data && Object.keys(res.data).length > 0) {
      Object.assign(form, res.data);
      isExist.value = true;
    }
  } catch (e) {
    if (e.response?.status === 401) {
      router.push({ name: "Login" });
    } else {
      error.value = "건강 정보를 불러오는 중 오류가 발생했습니다.";
    }
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
      await axios.put("/user/me/health", { ...form });
    } else {
      await axios.post("/user/me/health", { ...form });
    }
    router.push({ name: "ProfileInfo" });
  } catch (e) {
    if (e.response?.status === 401) {
      router.push({ name: "Login" });
    } else {
      error.value = e.response?.data?.message || "저장 중 문제가 발생했습니다.";
    }
  }
}
</script>

<style scoped>
.health-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 6rem;
  background: #f7fafc;
  box-sizing: border-box;
}

.health-card {
  background: #ffffff;
  width: 100%;
  max-width: 500px;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
}

h2 {
  font-size: 1.75rem;
  color: #333333;
  margin-bottom: 1rem;
  text-align: center;
}

.health-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-label {
  font-weight: 500;
  margin-bottom: 0.5rem;
  color: #333333;
}

.form-input,
.form-select {
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1rem;
  box-sizing: border-box;
}

.feedback.error {
  color: #d32f2f;
  font-size: 0.9rem;
  text-align: center;
}

.btn-primary {
  margin-top: 1rem;
  padding: 0.75rem;
  background-color: #21d59b;
  color: #ffffff;
  border: none;
  border-radius: 24px;
  font-size: 1rem;
  cursor: pointer;
  box-shadow: 0 2px 20px #21d59b15;
  transition: background-color 0.2s;
}
.btn-primary:hover {
  background-color: #1aa28a;
}

@media (max-width: 600px) {
  .health-wrapper {
    padding-top: 2rem;
    align-items: center;
  }
  .health-card {
    padding: 1.5rem;
    margin-top: 1rem;
  }
}
</style>
