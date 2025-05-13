<!-- src/views/UserInfoPage.vue -->
<template>
  <div class="info-page">
    <h2>개인 정보 수정</h2>
    <p class="greeting">{{ userNickname }}님, 정보 수정 페이지입니다.</p>

    <form @submit.prevent="onSubmit" class="info-form">
      <!-- 이메일 (읽기 전용) -->
      <label>
        이메일
        <input type="email" :value="form.email" disabled />
      </label>

      <!-- 닉네임 -->
      <label>
        닉네임
        <input v-model="form.nickname" @blur="checkNicknameDup" type="text" placeholder="닉네임 (최대 10자, 공백·특수문자 금지)" maxlength="10" required />
      </label>
      <p v-if="dupError.nickname" class="error">{{ dupError.nickname }}</p>

      <!-- 비밀번호 변경 (선택) -->
      <label>
        새 비밀번호
        <input v-model="form.password" type="password" placeholder="8~20자, 영문+특수문자" />
      </label>
      <label>
        비밀번호 확인
        <input v-model="form.passwordConfirm" type="password" placeholder="비밀번호 확인" />
      </label>

      <!-- 에러 메시지 -->
      <p v-if="error" class="error">{{ error }}</p>

      <button type="submit" :disabled="isCheckingDup">저장하기</button>
    </form>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();

// form state
const form = reactive({
  email: "",
  nickname: "",
  password: "",
  passwordConfirm: "",
});
const error = ref("");
const dupError = reactive({ nickname: "" });
const isCheckingDup = ref(false);
const userNickname = ref("");

// load existing user info
onMounted(async () => {
  try {
    const { data } = await axios.get("/api/user/profile");
    form.email = data.email;
    form.nickname = data.nickname;
    userNickname.value = data.nickname;
  } catch {
    error.value = "사용자 정보를 불러오는 중 오류가 발생했습니다.";
  }
});

// 닉네임 중복 검사
async function checkNicknameDup() {
  dupError.nickname = "";
  if (!form.nickname) return;
  const nickRe = /^[가-힣A-Za-z0-9]{1,10}$/;
  if (!nickRe.test(form.nickname)) {
    dupError.nickname = "닉네임은 최대10자, 공백·특수문자 없이 입력해주세요.";
    return;
  }
  if (form.nickname === userNickname.value) return;
  isCheckingDup.value = true;
  try {
    const res = await axios.get("/api/users/check-nickname", {
      params: { nickname: form.nickname },
    });
    if (!res.data.available) {
      dupError.nickname = "이미 사용 중인 닉네임입니다.";
    }
  } catch {
    dupError.nickname = "닉네임 확인에 실패했습니다.";
  } finally {
    isCheckingDup.value = false;
  }
}

// validate
function validate() {
  error.value = "";

  if (!form.nickname) {
    error.value = "닉네임을 입력해주세요.";
    return false;
  }
  if (dupError.nickname) {
    error.value = dupError.nickname;
    return false;
  }

  if (form.password) {
    const pwdRe = /^(?=.*[A-Za-z])(?=.*[^A-Za-z0-9]).{8,20}$/;
    if (!pwdRe.test(form.password)) {
      error.value = "비밀번호는 8~20자, 영문자+특수문자 조합이어야 합니다.";
      return false;
    }
    if (form.password !== form.passwordConfirm) {
      error.value = "비밀번호가 일치하지 않습니다.";
      return false;
    }
  }

  return true;
}

// submit
async function onSubmit() {
  if (!validate()) return;

  const payload = { nickname: form.nickname };
  if (form.password) payload.password = form.password;

  try {
    await axios.put("/api/user/profile", payload);
    alert("개인 정보가 저장되었습니다.");
    router.replace({ name: "ProfileInfo" });
  } catch (e) {
    error.value = e.response?.data?.message || "저장 중 오류가 발생했습니다.";
  }
}
</script>

<style scoped>
.info-page {
  max-width: 500px;
  margin: 2rem auto;
  padding: 2rem;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  text-align: center;
}

h2 {
  margin-bottom: 1rem;
}

.greeting {
  margin-bottom: 1.5rem;
  font-size: 1.1rem;
}

.info-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

label {
  display: flex;
  flex-direction: column;
  font-weight: 500;
}

input {
  margin-top: 0.5rem;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1rem;
}

input[disabled] {
  background: #f5f5f5;
  cursor: not-allowed;
}

button {
  margin-top: 1rem;
  padding: 0.75rem;
  border: none;
  border-radius: 6px;
  background: var(--orange-dark);
  color: #fff;
  cursor: pointer;
}

.error {
  color: #d32f2f;
  font-size: 0.9rem;
  text-align: left;
  margin: -0.5rem 0 0.5rem;
}
</style>
