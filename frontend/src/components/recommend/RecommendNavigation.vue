<template>
  <div class="recommend-navigation">
    <div class="mode-tabs">
      <button v-for="mode in availableModes" :key="mode.mode" :class="['mode-tab', { active: currentMode === mode.mode }]" @click="selectMode(mode.mode)">
        <span class="icon">{{ mode.icon }}</span>
        <span class="label">{{ mode.label }}</span>
        <span class="cost">🥄 {{ mode.cost }}</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useRecommendStore } from "@/stores/recommend";

const router = useRouter();
const store = useRecommendStore();

const availableModes = computed(() => store.availableModes);
const currentMode = computed(() => store.currentMode);

function selectMode(mode) {
  store.setCurrentMode(mode);
  router.push(`/recommend/${mode}`);
}

onMounted(() => {
  store.initializeModes();
});
</script>

<style scoped>
.recommend-navigation {
  margin-bottom: 20px;
}

.mode-tabs {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.mode-tab {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 100px;
}

.mode-tab:hover {
  border-color: #409eff;
  transform: translateY(-2px);
}

.mode-tab.active {
  border-color: #409eff;
  background: #f0f8ff;
}

.icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.label {
  font-weight: 600;
  margin-bottom: 2px;
}

.cost {
  font-size: 12px;
  color: #666;
}
</style>
