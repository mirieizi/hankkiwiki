<!-- src/components/login/FormPanel.vue -->
<template>
  <div class="form-panel">
    <h2>{{ title }}</h2>
    <p>{{ subtitle }}</p>

    <form @submit.prevent="onSubmit">
      <input v-if="!signIn" v-model.trim="form.nickname" @blur="checkNicknameDup" type="text" placeholder="닉네임 (최대10자, 공백·특수문자 금지)" maxlength="10" required />
      <p v-if="dupError.nickname" class="error">{{ dupError.nickname }}</p>
      <input v-model.trim="form.email" @blur="checkEmailDup" type="email" placeholder="이메일" required />
      <p v-if="dupError.email" class="error">{{ dupError.email }}</p>
      <input v-model="form.password" type="password" placeholder="비밀번호 (8~20자, 영문+특수문자)" required />
      <input v-if="!signIn" v-model="form.passwordConfirm" type="password" placeholder="비밀번호 확인" required />
      <p v-if="error" class="error">{{ error }}</p>

      <button type="submit" :disabled="isCheckingDup">
        {{ buttonText }}
      </button>
    </form>
    <a href="#" v-if="signIn">비밀번호를 까먹으셨나요?</a>
  </div>
</template>

<script setup>
import { reactive, ref, computed, defineProps } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";
axios.defaults.withCredentials = true;

const props = defineProps({
  signIn: { type: Boolean, default: true },
});

const router = useRouter();
const title = computed(() => (props.signIn ? "로그인" : "회원가입"));
const subtitle = computed(() => (props.signIn ? "한끼위키에 로그인하기!" : "새 계정을 만들어보세요!"));
const buttonText = computed(() => (props.signIn ? "로그인" : "회원가입"));

const form = reactive({
  nickname: "",
  email: "",
  password: "",
  passwordConfirm: "",
});
const error = ref("");

const dupError = reactive({ nickname: "", email: "" });
const isCheckingDup = ref(false);

async function checkNicknameDup() {
  dupError.nickname = "";
  if (!form.nickname) return;
  const nickRe = /^[가-힣A-Za-z0-9]{1,10}$/;
  if (!nickRe.test(form.nickname)) {
    dupError.nickname = "닉네임은 최대10자, 공백·특수문자 없이 입력해주세요.";
    return;
  }
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

async function checkEmailDup() {
  dupError.email = "";
  if (!form.email) return;
  const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRe.test(form.email)) {
    dupError.email = "유효한 이메일 주소가 아닙니다.";
    return;
  }
  if (props.signIn) return;
  isCheckingDup.value = true;
  try {
    const res = await axios.get("/api/users/check-email", {
      params: { email: form.email },
    });
    if (!res.data.available) {
      dupError.email = "이미 사용 중인 이메일입니다.";
    }
  } catch {
    dupError.email = "이메일 확인에 실패했습니다.";
  } finally {
    isCheckingDup.value = false;
  }
}

function validate() {
  error.value = "";

  if (!props.signIn) {
    if (!form.nickname) {
      error.value = "닉네임을 입력해주세요.";
      return false;
    }
    if (dupError.nickname) {
      error.value = dupError.nickname;
      return false;
    }
  }

  if (!form.email) {
    error.value = "이메일을 입력해주세요.";
    return false;
  }
  if (dupError.email) {
    error.value = dupError.email;
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

async function onSubmit() {
  if (!validate()) return;

  try {
    if (props.signIn) {
      const res = await axios.post("/api/auth/login", {
        email: form.email,
        password: form.password,
      });
      localStorage.setItem("token", res.data.token);
      await router.push("/");
    } else {
      await axios.post("/api/auth/signup", {
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
