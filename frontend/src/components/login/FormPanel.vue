<template>
  <div class="form-panel">
    <h2>{{ title }}</h2>
    <div class="social">
      <a v-for="net in socialNets" :key="net.icon" :href="net.href">{{ net.icon }}</a>
    </div>
    <p>한끼위키에 로그인하기!</p>
    <form>
      <input v-if="!signIn" type="text" placeholder="Name" />
      <input type="text" placeholder="Email" />
      <input type="password" placeholder="Password" />
      <button type="submit">{{ buttonText }}</button>
    </form>
    <a href="#" v-if="signIn">비밀번호를 까먹으셨나요??</a>
  </div>
</template>

<script setup>
import { computed, defineProps } from "vue";
const props = defineProps({ signIn: Boolean });
const title = computed(() => (props.signIn ? "Sign in" : "Create account"));
const buttonText = computed(() => (props.signIn ? "Sign in" : "Sign up"));
</script>

<style scoped>
form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin: 1rem 0;
}
input {
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 5px;
}
button[type="submit"] {
  padding: 0.75rem;
  border: none;
  border-radius: 5px;
  background: #4caf50;
  color: white;
  cursor: pointer;
}
.social a {
  margin: 0 0.5rem;
  font-size: 1.25rem;
}
</style>
