<!-- src/App.vue -->
<template>
  <BaseLayout>
    <!-- 라우팅된 페이지가 이 자리에 렌더링 됩니다 -->
    <router-view />
  </BaseLayout>
</template>

<script setup>
import BaseLayout from "@/components/BaseLayout.vue";
</script>

<style>
/* 전역 스타일이나 레이아웃 관련 스타일을 여기에 추가할 수도 있습니다 */
</style>
