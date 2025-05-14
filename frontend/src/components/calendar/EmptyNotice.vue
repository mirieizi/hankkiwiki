<!-- components/calendar/EmptyNotice.vue -->
<template>
  <div class="empty-notice">
    <p>해당 일자의 내용이 없습니다.</p>
  </div>
</template>

<script setup></script>

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
  align-items: center;
  justify-content: center;
}
</style>
