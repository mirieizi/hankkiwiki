<template>
  <div class="spinner-container">
    <!-- 왼쪽: 선택 결과 -->
    <div class="result-panel">
      <h2 v-if="selected" class="result-title">{{ selected.name }}</h2>
      <p v-if="selected" class="result-desc">{{ selected.detail }}</p>
      <button @click="spin" :disabled="spinning" class="spin-btn">
        {{ spinning ? "돌아가는 중…" : "랜덤 추천!" }}
      </button>
    </div>

    <!-- 오른쪽: 휠 스피너 -->
    <div class="wheel-wrapper">
      <!-- 포인터 -->
      <div class="pointer"></div>
      <!-- 회전할 원판 -->
      <div
        class="wheel"
        :style="{
          transform: `rotate(${rotation}deg)`,
          transition: spinning ? 'transform 4s cubic-bezier(0.33,1,0.68,1)' : 'none',
        }"
        @transitionend="onStop"
      >
        <div
          v-for="(food, i) in foods"
          :key="i"
          class="segment"
          :style="{
            transform: `rotate(${i * segmentAngle}deg) translate(0, -50%)`,
            background: food.color,
          }"
        >
          {{ food.name }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";

// 1) 음식 데이터(이름, 상세, 원판 색)
const foods = [
  { name: "김치찌개", detail: "매콤한 돼지고기 김치찌개", color: "#f28b82" },
  { name: "된장찌개", detail: "구수한 된장과 채소 듬뿍", color: "#fbbc04" },
  { name: "비빔밥", detail: "고추장 비빔밥 위에 계란후라이", color: "#34a853" },
  { name: "제육볶음", detail: "매콤달콤한 돼지 불고기", color: "#4285f4" },
  { name: "칼국수", detail: "쫄깃한 손칼국수 국물", color: "#9c27b0" },
  { name: "초밥", detail: "신선한 회와 밥 한입", color: "#03a9f4" },
];

const spinning = ref(false);
const rotation = ref(0);
const selected = ref(null);

const segmentAngle = computed(() => 360 / foods.length);

function spin() {
  if (spinning.value) return;
  spinning.value = true;
  selected.value = null;

  // 2) 랜덤 인덱스, 회전 계산
  const idx = Math.floor(Math.random() * foods.length);
  const extraTurns = Math.floor(Math.random() * 3) + 3; // 3~5 바퀴
  // 아이템 중앙을 포인터에 맞추려면 + segmentAngle/2
  const target = extraTurns * 360 + idx * segmentAngle.value + segmentAngle.value / 2;

  rotation.value += target;
}

function onStop() {
  // transition 끝나면 선택 결과 띄우기
  const normalized = rotation.value % 360;
  // pointer 기준으로 계산: 중앙 offset 만큼 뺌
  const idx = Math.floor(((normalized - segmentAngle.value / 2 + 360) % 360) / segmentAngle.value);
  selected.value = foods[idx];
  spinning.value = false;
}
</script>

<style scoped>
.spinner-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2rem;
  padding: 2rem;
}

/* 왼쪽 결과 패널 */
.result-panel {
  flex: 1 1 200px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 1rem;
}
.result-title {
  font-size: 1.8rem;
  margin: 0;
}
.result-desc {
  font-size: 1rem;
  color: #555;
}
.spin-btn {
  padding: 0.6rem 1.2rem;
  background: #34a853;
  color: #fff;
  border: none;
  border-radius: 0.5rem;
  cursor: pointer;
}
.spin-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 오른쪽 휠 */
.wheel-wrapper {
  position: relative;
  width: 250px;
  height: 250px;
}
.pointer {
  position: absolute;
  top: -10px;
  left: 50%;
  width: 0;
  height: 0;
  border-left: 12px solid transparent;
  border-right: 12px solid transparent;
  border-bottom: 20px solid #333;
  transform: translateX(-50%);
  z-index: 10;
}
.wheel {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  position: relative;
  overflow: hidden;
}
.segment {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 50%;
  height: 50%;
  transform-origin: 0 0;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding-left: 8px;
  font-size: 0.9rem;
  color: #fff;
  font-weight: bold;
}
</style>
