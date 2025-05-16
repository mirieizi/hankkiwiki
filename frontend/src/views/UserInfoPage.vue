<script setup>
import { ref, watchEffect, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const userStore = useUserStore();

// 로컬 폼 상태
const form = ref({
  email: "",
  nickname: "",
  password: "",
  passwordConfirm: "",
});
const isChecking = ref(false);

// 초기 데이터 로드
onMounted(async () => {
  await userStore.fetchProfile();
  form.value.email = userStore.email;
  form.value.nickname = userStore.nickname;
});

// 닉네임 입력할 때마다 Pinia로 검사
watchEffect(async () => {
  const nick = form.value.nickname.trim();
  if (!nick || nick === userStore.nickname) {
    userStore.dupError = "";
    return;
  }
  isChecking.value = true;
  await userStore.checkNickname(nick, userStore.nickname);
  isChecking.value = false;
});

// 유효성 검사
function validate() {
  if (userStore.dupError) return false;
  const pwd = form.value.password;
  if (pwd) {
    const pwdRe = /^(?=.*[A-Za-z])(?=.*[^A-Za-z0-9]).{8,20}$/;
    if (!pwdRe.test(pwd) || pwd !== form.value.passwordConfirm) {
      userStore.error = pwdRe.test(pwd) ? "비밀번호가 일치하지 않습니다." : "비밀번호는 8~20자, 영문자+특수문자 조합이어야 합니다.";
      return false;
    }
  }
  return true;
}

// 제출
async function onSubmit() {
  if (!validate()) return;
  const payload = { nickname: form.value.nickname };
  if (form.value.password) payload.password = form.value.password;

  const ok = await userStore.updateProfile(payload);
  if (ok) {
    alert("개인 정보가 저장되었습니다.");
    router.replace({ name: "ProfileInfo" });
  }
}
</script>

<template>
  <div class="info-page">
    <h2>개인 정보 수정</h2>
    <p class="greeting">{{ userStore.nickname }}님, 정보 수정 페이지입니다.</p>

    <form @submit.prevent="onSubmit" class="info-form">
      <label>
        이메일
        <input type="email" :value="form.email" disabled />
      </label>

      <label>
        닉네임
        <input v-model="form.nickname" type="text" placeholder="닉네임 (최대 10자, 공백·특수문자 금지)" maxlength="10" required />
      </label>
      <p v-if="userStore.dupError" class="error">{{ userStore.dupError }}</p>

      <label>
        새 비밀번호
        <input v-model="form.password" type="password" placeholder="8~20자, 영문+특수문자" />
      </label>
      <label>
        비밀번호 확인
        <input v-model="form.passwordConfirm" type="password" placeholder="비밀번호 확인" />
      </label>

      <p v-if="userStore.error" class="error">{{ userStore.error }}</p>

      <button type="submit" :disabled="isChecking">저장하기</button>
    </form>
  </div>
</template>

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
