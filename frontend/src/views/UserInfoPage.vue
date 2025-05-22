<!-- src/views/UserInfoPage.vue -->
<script setup>
import { ref, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const authStore = useAuthStore();

const form = ref({
  email: "",
  nickname: "",
  password: "",
  passwordConfirm: "",
});
const isChecking = ref(false);
const nicknameChecked = ref(true);

// 최초 정보 로드 (authStore에 내 정보 fetchProfile로 저장해놓는다고 가정)
onMounted(async () => {
  await authStore.fetchProfile?.(); // 이 함수가 없으면 빼도 됨 (authStore.userInfo가 이미 세팅된 상태면 필요 없음)
  // userInfo에 있는 값으로 form 세팅
  form.value.email = authStore.userInfo?.email || "";
  form.value.nickname = authStore.userInfo?.nickname || "";
  nicknameChecked.value = true;
  authStore.dupError = "";
});

// 닉네임 입력 변경 시 중복체크 flag 초기화
watch(
  () => form.value.nickname,
  (newNick) => {
    nicknameChecked.value = newNick === (authStore.userInfo?.nickname || "");
    authStore.dupError = "";
  }
);

// 닉네임 중복 확인 버튼 이벤트
async function checkNicknameDup() {
  authStore.dupError = "";
  nicknameChecked.value = false;
  const nick = form.value.nickname.trim();
  if (!nick) return;
  if (nick === (authStore.userInfo?.nickname || "")) {
    nicknameChecked.value = true;
    return;
  }
  isChecking.value = true;
  await authStore.checkNickname(nick);
  if (!authStore.dupError) nicknameChecked.value = true;
  isChecking.value = false;
}

// 유효성 검사
function validate() {
  if (!nicknameChecked.value) {
    authStore.error = "닉네임 중복 확인을 해주세요.";
    return false;
  }
  if (authStore.dupError) return false;
  const pwd = form.value.password;
  if (pwd) {
    const pwdRe = /^(?=.*[A-Za-z])(?=.*[^A-Za-z0-9]).{8,20}$/;
    if (!pwdRe.test(pwd) || pwd !== form.value.passwordConfirm) {
      authStore.error = pwdRe.test(pwd) ? "비밀번호가 일치하지 않습니다." : "비밀번호는 8~20자, 영문자+특수문자 조합이어야 합니다.";
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

  // authStore에 updateProfile 구현되어 있다고 가정
  const ok = await authStore.updateProfile(payload);
  if (ok) {
    alert("개인 정보가 저장되었습니다.");
    router.replace({ name: "ProfileInfo" });
  }
}
</script>

<template>
  <div class="info-page">
    <h2>개인 정보 수정</h2>
    <p class="greeting">{{ authStore.userInfo?.nickname }}님, 정보 수정 페이지입니다.</p>

    <form @submit.prevent="onSubmit" class="info-form">
      <label>
        이메일
        <input type="email" :value="form.email" disabled />
      </label>

      <label>
        닉네임
        <div style="display: flex; gap: 0.5rem">
          <input v-model="form.nickname" type="text" placeholder="닉네임 (최대 10자, 공백·특수문자 금지)" maxlength="10" required />
          <button type="button" @click="checkNicknameDup" :disabled="isChecking || !form.nickname">중복 확인</button>
        </div>
      </label>
      <p v-if="authStore.dupError" class="error">{{ authStore.dupError }}</p>
      <p v-else-if="nicknameChecked" class="success">사용 가능한 닉네임입니다.</p>

      <label>
        새 비밀번호
        <input v-model="form.password" type="password" placeholder="8~20자, 영문+특수문자" />
      </label>
      <label>
        비밀번호 확인
        <input v-model="form.passwordConfirm" type="password" placeholder="비밀번호 확인" />
      </label>

      <p v-if="authStore.error" class="error">{{ authStore.error }}</p>

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
.success {
  color: #4caf50;
  font-size: 0.9rem;
  text-align: left;
  margin: -0.5rem 0 0.5rem;
}
</style>
