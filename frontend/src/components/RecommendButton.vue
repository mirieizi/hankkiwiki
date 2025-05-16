<template>
  <button class="recommend-btn" :disabled="spoonCount < cost || loading" @click="$emit('run')">
    <div class="label">{{ label }} (숟가락 {{ cost }}개)</div>
    <div class="spoon">🥄 {{ spoonCount }}/5</div>
  </button>
</template>

<script>
export default {
  name: "RecommendButton",
  props: {
    label: { type: String, required: true },
    cost: { type: Number, required: true },
    spoonCount: { type: Number, required: true },
    loading: { type: Boolean, default: false },
  },
};
</script>

<style scoped>
.recommend-btn {
  border: 2px solid #409eff;
  border-radius: 8px;
  background: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  cursor: pointer;
}
.recommend-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.label {
  flex: 1;
}
.spoon {
  font-size: 14px;
}
</style>
