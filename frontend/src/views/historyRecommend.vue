<!-- src/views/AiRecommend.vue -->
<template>
  <Recommend initialMode="history" />
</template>

<script>
import Recommend from "@/components/recommend/Recommend.vue";

export default {
  name: "AiRecommend",
  components: { Recommend },
};
</script>

<style scoped>
/* 필요시 뷰 전용 커스텀 스타일 추가 (지금은 Recommend.vue 에서 전부 처리) */
</style>
