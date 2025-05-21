<template>
  <div class="form-panel">
    <h2>{{ title }}</h2>
    <p>{{ subtitle }}</p>

    <form @submit.prevent="onSubmit">
      <!-- 닉네임 (회원가입 모드에서만) -->
      <template v-if="!signIn">
        <div class="field-group">
          <input v-model.trim="form.nickname" type="text" placeholder="닉네임 (최대10자, 공백·특수문자 금지)" maxlength="10" required :disabled="isCheckingDup" />
          <button type="button" @click="onCheckEmailDup" :disabled="isCheckingDup || !form.nickname">중복 확인</button>
        </div>
        <p v-if="dupError.nickname" class="error">{{ dupError.nickname }}</p>
        <p v-else-if="nicknameChecked" class="success">사용 가능한 닉네임입니다.</p>
      </template>

      <!-- 이메일 -->
      <div class="field-group">
        <input v-model.trim="form.email" type="email" placeholder="이메일" required :disabled="isCheckingDup" />
        <button v-if="!signIn" type="button" @click="checkEmailDup" :disabled="isCheckingDup || !form.email">중복 확인</button>
      </div>
      <p v-if="dupError.email" class="error">{{ dupError.email }}</p>
      <p v-else-if="emailChecked" class="success">사용 가능한 이메일입니다.</p>

      <!-- 비밀번호 -->
      <input v-model="form.password" type="password" placeholder="비밀번호 (8~20자, 영문+특수문자)" required />
      <input v-if="!signIn" v-model="form.passwordConfirm" type="password" placeholder="비밀번호 확인" required />
      <p v-if="error" class="error">{{ error }}</p>

      <!-- 제출 버튼 -->
      <button type="submit" :disabled="isCheckingDup || (!signIn && (!nicknameChecked || !emailChecked))">
        {{ buttonText }}
      </button>
    </form>

    <a href="#" v-if="signIn">비밀번호를 까먹으셨나요?</a>
  </div>
</template>

<script setup>
import { reactive, ref, computed, defineProps } from "vue";
import { useRouter } from "vue-router";
import axios from "@/plugins/axios";
import { useAuthStore } from "@/stores/auth";
const authStore = useAuthStore();

const props = defineProps({ signIn: { type: Boolean, default: true } });
const router = useRouter();

// Form 데이터 & 상태
const form = reactive({
  nickname: "",
  email: "",
  password: "",
  passwordConfirm: "",
});
const error = ref("");
const dupError = reactive({ nickname: "", email: "" });
const isCheckingDup = ref(false);

// 중복 확인 플래그
const nicknameChecked = ref(false);
const emailChecked = ref(false);

// Computed: 타이틀, 버튼 텍스트
const title = computed(() => (props.signIn ? "로그인" : "회원가입"));
const subtitle = computed(() => (props.signIn ? "한끼위키에 로그인하기!" : "새 계정을 만들어보세요!"));
const buttonText = computed(() => (props.signIn ? "로그인" : "회원가입"));

// 닉네임 중복 확인
async function onCheckEmailDup() {
  await userStore.checkEmailDup(form.email);
}

// 이메일 중복 확인
async function checkEmailDup() {
  dupError.email = "";
  emailChecked.value = false;
  if (!form.email) return;
  const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRe.test(form.email)) {
    dupError.email = "유효한 이메일 주소가 아닙니다.";
    return;
  }
  isCheckingDup.value = true;
  try {
    const res = await axios.get("/auth/check-email", {
      params: { email: form.email },
    });
    if (res.data.available) emailChecked.value = true;
    else dupError.email = "이미 사용 중인 이메일입니다.";
  } catch {
    dupError.email = "이메일 확인에 실패했습니다.";
  } finally {
    isCheckingDup.value = false;
  }
}

// Validation
function validate() {
  error.value = "";
  if (!props.signIn) {
    if (!form.nickname) {
      error.value = "닉네임을 입력해주세요.";
      return false;
    }
    if (!nicknameChecked.value) {
      error.value = "닉네임 중복 확인을 해주세요.";
      return false;
    }
  }
  if (!form.email) {
    error.value = "이메일을 입력해주세요.";
    return false;
  }
  if (!props.signIn && !emailChecked.value) {
    error.value = "이메일 중복 확인을 해주세요.";
    return false;
  }

  const pwdRe = /^(?=.*[A-Za-z])(?=.*[^A-Za-z0-9]).{8,20}$/;
  if (!pwdRe.test(form.password)) {
    error.value = "비밀번호는 8~20자, 영문자와 특수문자를 포함해야 합니다.";
    return false;
  }
  if (!props.signIn && form.password !== form.passwordConfirm) {
    error.value = "비밀번호가 일치하지 않습니다.";
    return false;
  }
  return true;
}

// Submit 핸들러
async function onSubmit() {
  if (!validate()) return;
  try {
    if (props.signIn) {
      await authStore.login({
        email: form.email,
        password: form.password,
      });
      // (login 액션 내부에서 자동으로 토큰저장, 프로필불러오기, 리다이렉트까지 처리)
    } else {
      // 회원가입은 필요에 따라 authStore.signup 등도 만들면 좋음
      await axios.post("/auth/signup", {
        nickname: form.nickname,
        email: form.email,
        password: form.password,
      });
      alert("회원가입 성공! 로그인 페이지로 이동합니다.");
      await router.push("/login");
    }
  } catch (e) {
    error.value = e.response?.data?.message || "요청 중 오류가 발생했습니다.";
  }
}
</script>

<style scoped>
.form-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
}
p {
  margin: 0.5rem 0;
}
form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin: 1rem 0;
}
input {
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 5px;
  width: 100%;
}
button[type="submit"] {
  padding: 0.75rem;
  border: none;
  border-radius: 5px;
  background: #4caf50;
  color: white;
  cursor: pointer;
}
.error {
  color: #d32f2f;
  font-size: 0.9rem;
  text-align: center;
}
</style>
