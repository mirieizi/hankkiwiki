<template>
  <div class="action-panel">
    <img class="mascot" :src="props.signIn ? scholarBear : welcomeBear" alt="한 끼 천재 마스코트" />

    <h2 class="diary-font">{{ heading }}</h2>
    <p>{{ text }}</p>
    <button @click.prevent="$emit('slide')">{{ btn }}</button>
  </div>
</template>

<script setup>
import { computed, defineProps } from "vue";
import scholarBear from "@/assets/logo2.png";
import welcomeBear from "@/assets/welcome_logo.png";

const props = defineProps({ signIn: Boolean });

const heading = computed(() => (props.signIn ? "반가워요!" : "환영해요! :"));
const text = computed(() =>
  props.signIn
    ? "어서 오세요, 식탁의 고민 해결사! 방대한 음식 영양 백과와 나만의 식단 분석으로 ‘오늘의 한 끼’를 스마트하게 골라드립니다."
    : "지금 가입하고 ‘한 끼 천재’ 되기! 음식 영양 사전부터 개인 맞춤 메뉴 추천까지, 맛과 건강을 한 번에 사로잡을 준비 되셨나요?"
);
const btn = computed(() => (props.signIn ? "회원가입하기" : "로그인하기"));
</script>

<style scoped>
.action-panel {
  background: #6faf9b;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 28px;
  padding: 40px 32px 32px 32px;
}

.mascot {
  display: block;
  width: 150px;
  margin: 0 auto 18px;
  border-radius: 25px;
}

.diary-font {
  font-size: 2rem;
  font-weight: 800;
  color: #fcfcfc;
  text-align: center;
  margin-bottom: 0.35em;
  letter-spacing: 0.5px;
}
p {
  color: #ffffff;
  font-size: 1.05rem;
  text-align: center;
  margin: 0 0 18px 0;
  line-height: 1.6;
}

button {
  display: inline-block;
  min-width: 100px;
  padding: 0.7rem 2.1rem;
  background: #fff;
  color: #21d59b; /* 더 맑은 민트톤 추천 */
  font-size: 1.04rem;
  font-weight: 600;
  border: none; /* <-- 테두리 삭제! */
  border-radius: 18px;
  box-shadow: none;
  transition: background 0.15s, color 0.15s;
  margin: 0 auto;
  outline: none;
}
button:hover {
  background: #ffffff10;
  color: #21d59b;
  border-color: #15b382;
}
</style>
