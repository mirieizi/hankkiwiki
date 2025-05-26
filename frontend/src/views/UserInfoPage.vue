<template>
  <div class="info-wrapper">
    <div class="info-card">
      <h2>개인 정보 수정</h2>
      <p class="greeting">{{ form.nickname || authStore.userInfo?.nickname }}님, 정보 수정 페이지입니다.</p>

      <form @submit.prevent="onSubmit" class="info-form">
        <div class="form-group">
          <label class="form-label">이메일</label>
          <input type="email" :value="form.email" disabled class="form-input" />
        </div>

        <div class="form-group flex-row">
          <div class="flex-grow">
            <label class="form-label">닉네임</label>
            <br />
            <br />
            <input v-model="form.nickname" type="text" placeholder="닉네임 (최대 10자, 공백·특수문자 금지)" maxlength="10" required class="form-input" />
          </div>
          <button type="button" @click="checkNicknameDup" :disabled="isChecking || !form.nickname" class="btn-outline">중복 확인</button>
        </div>
        <p v-if="authStore.dupError" class="feedback error">{{ authStore.dupError }}</p>
        <p v-else-if="nicknameChecked" class="feedback success">사용 가능한 닉네임입니다.</p>

        <div class="form-group">
          <label class="form-label">새 비밀번호</label>
          <input v-model="form.password" type="password" placeholder="8~20자, 영문+특수문자" class="form-input" />
        </div>

        <div class="form-group">
          <label class="form-label">비밀번호 확인</label>
          <input v-model="form.passwordConfirm" type="password" placeholder="비밀번호 확인" class="form-input" />
        </div>

        <p v-if="authStore.error" class="feedback error">{{ authStore.error }}</p>

        <button type="submit" :disabled="isChecking" class="btn-primary">저장하기</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const authStore = useAuthStore();

const form = ref({ email: "", nickname: "", password: "", passwordConfirm: "" });
const isChecking = ref(false);
const nicknameChecked = ref(true);

onMounted(async () => {
  await authStore.fetchProfile?.();
  form.value.email = authStore.userInfo?.email || "";
  form.value.nickname = authStore.userInfo?.nickname || "";
  nicknameChecked.value = true;
  authStore.dupError = "";
});

watch(
  () => form.value.nickname,
  (newNick) => {
    nicknameChecked.value = newNick === (authStore.userInfo?.nickname || "");
    authStore.dupError = "";
  }
);

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

async function onSubmit() {
  if (!validate()) return;
  const payload = { nickname: form.value.nickname };
  if (form.value.password) payload.password = form.value.password;
  const ok = await authStore.updateProfile(payload);
  if (ok) {
    alert("개인 정보가 저장되었습니다.");
    router.replace({ name: "ProfileInfo" });
  }
}
</script>

<style scoped>
.info-wrapper {
  width: 100%;
  /* 부모 컨테이너 기준 높이에 따라 자동으로 늘어나도록 수정 */
  height: 100%;
  min-height: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f7fafc;
  padding: 2rem 1rem;
  box-sizing: border-box;
  /* 카드 시작 위치를 상단에서 동일하게 맞추기 위해 flex-start */
  align-items: flex-start;
  /* 상단 여백 조정 (헤더 높이 + 추가 마진) */
  padding-top: 6rem;
  background: #f7fafc;
  box-sizing: border-box;
}

.info-card {
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
  margin-bottom: 0.5rem;
  text-align: center;
}

.greeting {
  font-size: 1.125rem;
  color: #666666;
  margin-bottom: 1rem;
  text-align: center;
}

.info-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.flex-row {
  flex-direction: row;
  align-items: flex-end;
}

.flex-grow {
  flex: 1;
}

.form-label {
  font-weight: 500;
  margin-bottom: 0.5rem;
  color: #333333;
}

.form-input {
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1rem;
  box-sizing: border-box;
}

.form-input[disabled] {
  background: #f5f5f5;
  cursor: not-allowed;
}

.btn-outline {
  margin-left: 0.5rem;
  padding: 0.65rem 1rem;
  border: 1px solid #21d59b;
  border-radius: 24px;
  background: none;
  color: #21d59b;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s;
}
.btn-outline:hover {
  background-color: #21d59b;
  color: #ffffff;
}

.feedback {
  font-size: 0.9rem;
  margin-top: -0.75rem;
  margin-left: 0.25rem;
  text-align: left;
}
.feedback.error {
  color: #d32f2f;
}
.feedback.success {
  color: #4caf50;
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
.btn-primary:disabled {
  background-color: #c6f1e5;
  cursor: not-allowed;
}
.btn-primary:hover:enabled {
  background-color: #1aa28a;
}
</style>
