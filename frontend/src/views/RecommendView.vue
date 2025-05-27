<template>
  <Recommend :initialMode="mode" />
</template>

<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";
import Recommend from "@/components/recommend/Recommend.vue";

const route = useRoute();

const mode = computed(() => {
  const validModes = ["ai", "custom", "history", "random"];
  return validModes.includes(route.params.mode) ? route.params.mode : "random";
});
</script>
