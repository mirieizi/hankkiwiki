<!-- components/calendar/EmptyNotice.vue -->
<template>
  <div class="empty-notice">
    <p>해당 일자의 내용이 없습니다.</p>
    <button type="button" @click="goToRegister">기록하러 가기</button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

// 상위에서 selectedDate를 props로 내려보냈다고 가정
const props = defineProps({
  date: {
    type: String,
    required: true,
  },
});
function goToRegister() {
  if (!props.date) {
    console.warn('EmptyNotice: 날짜 정보 없음. 이동 중단');
    return;
  }

  router.push({
    path: '/food/register',
    query: { date: props.date }, // ex: ?date=2025-05-14
  });
}
</script>

<style scoped>
.empty-notice {
  background-color: #fff;
  padding: 2rem;
  border-radius: 12px;
  text-align: center;
  color: #999;
  font-size: 1rem;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  min-height: 300px;

  display: flex;
  flex-direction: column; /* ✅ 위아래 정렬 */
  align-items: center;
  justify-content: center;
  gap: 1rem; /* ✅ 텍스트와 버튼 간격 */
}

.empty-notice button {
  padding: 0.6rem 1.4rem;
  border: none;
  border-radius: 10px;
  background-color: #ffe2b3;
  color: #5c3b1e;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  transform: scale(1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.empty-notice button:hover {
  background-color: #ffd088;
  transform: scale(1.03);
}
</style>
