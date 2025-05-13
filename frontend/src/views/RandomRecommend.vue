<!-- src/views/RandomRecommend.vue -->
<template>
  <div class="random-page">
    <h2>‘{{ userNickname }}’님을 위한 오늘의 랜덤 추천 🎉</h2>

    <div class="wheel-container">
      <!-- 회전하는 원판 -->
      <div class="wheel" :style="{ transform: `rotate(${currentDeg}deg)` }" @transitionend="onSpinEnd">
        <div
          v-for="(item, idx) in items"
          :key="idx"
          class="slice"
          :style="{
            transform: `rotate(${idx * sliceDeg}deg)`,
            background: sliceColors[idx % sliceColors.length],
          }"
        >
          <span class="label">{{ item }}</span>
        </div>
      </div>
      <!-- 고정된 포인터 -->
      <div class="pointer"></div>
    </div>

    <button @click="spin" :disabled="isSpinning || spinsLeft <= 0">
      {{ isSpinning ? "빙글빙글..." : "랜덤으로 추천받기" }}
    </button>
    <p class="spins-left">(남은 추천 횟수: {{ spinsLeft }})</p>

    <transition name="fade">
      <div class="result" v-if="result">
        <p>🎊 오늘의 추천 메뉴는</p>
        <h3>{{ result }} 🍽️</h3>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";

const userNickname = ref("손님");
const spinsLeft = ref(5);

// 테스트용 사용자 정보 로드 (실제 API 호출로 대체 가능)
onMounted(() => {
  // 예시: userNickname.value = 실제 API에서 받아온 닉네임
  // spinsLeft.value = API 에서 받아온 남은 횟수
});

const items = ["비빔밥", "된장찌개", "치킨", "샐러드", "스테이크", "파스타"];

const sliceDeg = 360 / items.length; // 한 조각 각도
const sliceColors = ["#FFD7BA", "#FFEFCF", "#D1FFD1", "#CFFFEF", "#D1E0FF", "#FFD1E0"];

const currentDeg = ref(0);
const isSpinning = ref(false);
const result = ref(null);

function spin() {
  if (isSpinning.value || spinsLeft.value <= 0) return;
  isSpinning.value = true;
  result.value = null;
  spinsLeft.value--;

  const pick = Math.floor(Math.random() * items.length);
  const rounds = 360 * 5; // 5바퀴 돌리고
  // 포인터(위쪽 12시 방향)에 맞추기 위해 보정
  const offset = 90 - sliceDeg / 2;
  const target = rounds + pick * sliceDeg * -1 + offset;
  currentDeg.value += target;
}

function onSpinEnd() {
  isSpinning.value = false;
  const norm = ((currentDeg.value % 360) + 360) % 360;
  // 12시 포인터 아래에 위치한 조각 인덱스 계산
  const idx = Math.floor((norm + sliceDeg / 2) / sliceDeg) % items.length;
  result.value = items[(items.length - idx) % items.length];
}
</script>

<style scoped>
.random-page {
  max-width: 600px;
  margin: 2rem auto;
  text-align: center;
  color: #333;
  font-family: sans-serif;
}

.wheel-container {
  position: relative;
  margin-top: 10%;
  width: 320px;
  height: 320px;
  margin: 1.5rem auto;
}

.wheel {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  position: relative;
  margin-top: 2.5cm;
  transition: transform 4s cubic-bezier(0.33, 1, 0.68, 1);
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.2);
}

/* 6조각 */
.slice {
  position: absolute;
  width: 50%;
  height: 50%;
  top: 0;
  left: 50%;
  transform-origin: 0% 100%;
  overflow: hidden;
  border: 1px solid #fff;
}

.label {
  position: absolute;
  width: 100%;
  text-align: center;
  transform: rotate(90deg) translateY(-60%);
  transform-origin: center;
  font-weight: bold;
  color: #444;
  user-select: none;
}

.pointer {
  position: absolute;
  top: -10px;
  left: 50%;
  margin-left: -10px;
  width: 0;
  height: 0;
  border-left: 10px solid transparent;
  border-right: 10px solid transparent;
  border-bottom: 20px solid #dc6936;
}

button {
  padding: 0.75rem 1.5rem;
  margin-top: 3cm;
  background: #dc6936;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
button[disabled] {
  opacity: 0.5;
  cursor: default;
}

.spins-left {
  margin-top: 0.5rem;
  color: #666;
}

.result {
  margin-top: 1.5rem;
  padding: 1rem;
  background: #ffe2b3;
  border-radius: 8px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
