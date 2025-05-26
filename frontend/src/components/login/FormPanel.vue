<template>
  <div class="form-panel">
    <h2>{{ title }}</h2>
    <p class="subtitle">{{ subtitle }}</p>

    <form @submit.prevent="onSubmit">
      <!-- 닉네임 (회원가입 모드) -->
      <template v-if="!signIn">
        <div class="field-group">
          <input v-model.trim="form.nickname" type="text" placeholder="닉네임 (최대10자, 공백·특수문자 금지)" maxlength="10" required :disabled="authStore.isCheckingNickname" />
          <button type="button" @click="checkNicknameDup" :disabled="authStore.isCheckingNickname || !form.nickname">중복 확인</button>
        </div>
        <p v-if="authStore.nicknameDupError" class="error">
          {{ authStore.nicknameDupError }}
        </p>
        <p v-else-if="authStore.nicknameChecked" class="success">사용 가능한 닉네임입니다.</p>
      </template>

      <!-- 이메일 -->
      <div class="field-group">
        <input v-model.trim="form.email" type="email" placeholder="이메일" required :disabled="authStore.isCheckingEmail" />
        <button v-if="!signIn" type="button" @click="checkEmailDup" :disabled="authStore.isCheckingEmail || !form.email">중복 확인</button>
      </div>
      <p v-if="authStore.emailDupError" class="error">
        {{ authStore.emailDupError }}
      </p>
      <p v-else-if="authStore.emailChecked" class="success">사용 가능한 이메일입니다.</p>

      <!-- 비밀번호 -->
      <input v-model="form.password" type="password" placeholder="비밀번호 (8~20자, 영문+특수문자)" required />
      <input v-if="!signIn" v-model="form.passwordConfirm" type="password" placeholder="비밀번호 확인" required />
      <p v-if="error" class="error">{{ error }}</p>

      <!-- 제출 버튼 -->
      <button type="submit" :disabled="authStore.isCheckingNickname || authStore.isCheckingEmail || (!signIn && (!authStore.nicknameChecked || !authStore.emailChecked))">
        {{ buttonText }}
      </button>
    </form>

    <a href="#" v-if="signIn" class="forgot-link">비밀번호를 까먹으셨나요?</a>
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

const form = reactive({
  nickname: "",
  email: "",
  password: "",
  passwordConfirm: "",
});
const error = ref("");

const title = computed(() => (props.signIn ? "로그인" : "회원가입"));
const subtitle = computed(() => (props.signIn ? "한끼위키에 로그인하기!" : "새 계정을 만들어보세요!"));
const buttonText = computed(() => (props.signIn ? "로그인" : "회원가입"));

async function checkNicknameDup() {
  await authStore.checkNickname(form.nickname);
}
async function checkEmailDup() {
  await authStore.checkEmailDup(form.email);
}
function validate() {
  error.value = "";
  if (!props.signIn) {
    if (!form.nickname) {
      error.value = "닉네임을 입력해주세요.";
      return false;
    }
    if (!authStore.nicknameChecked) {
      error.value = "닉네임 중복 확인을 해주세요.";
      return false;
    }
  }
  if (!form.email) {
    error.value = "이메일을 입력해주세요.";
    return false;
  }
  if (!props.signIn && !authStore.emailChecked) {
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
async function onSubmit() {
  if (!validate()) return;
  try {
    if (props.signIn) {
      await authStore.login({
        email: form.email,
        password: form.password,
      });
    } else {
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
  background: #fff;
  border-radius: 36px;
  box-shadow: 0 6px 40px 0 #20c59c18, 0 1.5px 12px 0 #fff7e3;
  padding: 44px 36px 36px 36px;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: box-shadow 0.19s cubic-bezier(0.33, 1, 0.68, 1);
}
.form-panel:focus-within,
.form-panel:hover {
  box-shadow: 0 8px 44px 0 #20c59c33, 0 4px 32px #ffc83d1a;
}

h2 {
  color: #21d59b;
  font-size: 2.1rem;
  font-weight: 800;
  margin-bottom: 12px;
  letter-spacing: 1px;
}
.subtitle {
  color: #94b9b5;
  font-size: 1.05rem;
  margin-bottom: 0.7rem;
  font-weight: 600;
  text-align: center;
}
form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin: 1.6rem 0 1rem 0;
}
.field-group {
  display: flex;
  gap: 8px;
  align-items: center;
}
input {
  flex: 1;
  padding: 0.9rem 1.15rem;
  border: none;
  border-radius: 18px;
  background: #f5fffa;
  font-size: 1.1rem;
  outline: none;
  font-family: inherit;
  transition: box-shadow 0.14s, background 0.17s;
  box-shadow: 0 1.5px 6px #21d59b13;
}
input:focus {
  box-shadow: 0 4px 16px #21d59b22;
  background: #eafff7;
}

button[type="button"] {
  background: #fff;
  border: 2px solid #ffc83d;
  color: #ffc83d;
  padding: 0.7rem 1rem;
  border-radius: 14px;
  font-weight: 700;
  font-size: 1rem;
  transition: 0.13s;
  cursor: pointer;
  min-width: 80px;
  box-shadow: 0 1.5px 7px #ffc83d11;
}
button[type="button"]:hover:enabled {
  background: #ffc83d;
  color: #fff;
  border-color: #ffc83d;
}

button[type="submit"] {
  padding: 1rem 0;
  background: linear-gradient(90deg, #21d59b 0%, #6faf9b 100%);
  color: #fff;
  border: none;
  border-radius: 23px;
  font-size: 1.15rem;
  font-weight: 800;
  cursor: pointer;
  box-shadow: 0 2px 20px #21d59b15;
  margin-top: 0.5rem;
  transition: 0.18s;
}
button[type="submit"]:disabled {
  background: #c6f1e5;
  color: #fff;
  cursor: not-allowed;
  box-shadow: none;
}
button[type="submit"]:hover:enabled {
  background: linear-gradient(90deg, #20c59c 0%, #ffc83d 100%);
}

.error {
  color: #ef6c60;
  font-size: 0.97rem;
  text-align: center;
  margin: -12px 0 0;
  font-weight: 700;
}
.success {
  color: #21d59b;
  font-size: 0.97rem;
  text-align: center;
  margin: -12px 0 0;
  font-weight: 700;
}

.forgot-link {
  color: #ffc83d;
  text-decoration: underline dashed;
  margin-top: 18px;
  font-size: 1rem;
  font-weight: 600;
  letter-spacing: 0.1px;
  transition: color 0.12s;
}
.forgot-link:hover {
  color: #ff9241;
}

/* 반응형 조정 */
@media (max-width: 500px) {
  .form-panel {
    padding: 24px 8px 18px 8px;
    width: 100%;
    height: auto;
    border-radius: 18px;
  }
  h2 {
    font-size: 1.25rem;
  }
}
</style>
