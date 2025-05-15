<template>
  <div>
    <h3>🍴 선택된 음식</h3>
    <ul class="selected-list">
      <li v-for="food in foods" :key="food" class="selected-item">
        {{ food }}
        <button
          class="remove-button"
          @click="$emit('remove-food', food)"
          aria-label="삭제"
        >
          <img src="@/assets/junk.png" alt="삭제" class="remove-icon" />
        </button>
      </li>
    </ul>
    <button class="save-button" @click="saveFoods">저장하기</button>
  </div>
</template>

<script setup>
const props = defineProps({
  foods: {
    type: Array,
    required: true,
  },
});

function saveFoods() {
  console.log('✅ 저장된 음식 목록:', props.foods);
  alert('음식이 저장되었습니다!');
}
</script>

<style scoped>
.selected-list {
  margin-top: 2rem;
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.selected-item {
  background-color: #ffe9b5;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remove-button {
  background: none;
  border: none;
  padding: 0.2rem;
  cursor: pointer;
  border-radius: 50%;
  transition: background-color 0.2s ease;
}

.remove-button:hover {
  background-color: #fff0c1;
}

.remove-icon {
  width: 24px;
  height: 24px;
}

.save-button {
  background-color: #ffe9b5;
  border: none;
  padding: 0.7rem 1rem;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: 0.2s;
  width: 100%;
  box-sizing: border-box;
  font-size: 1rem;
  margin-top: 1.2rem;
}

.save-button:hover {
  background-color: #ffd983;
}
</style>
