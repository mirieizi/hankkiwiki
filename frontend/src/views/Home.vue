<template>
  <div class="home-container">
    <div class="mascot-box">
      <div class="mascot-face">🍚</div>
    </div>
    <h1 class="title">한끼위키</h1>
    <p class="subtitle">
      오늘 뭐 먹을지 고민될 때,
      <span class="point">한끼위키</span>
      !
    </p>
    <div class="button-group">
      <button class="main-btn">오늘의 추천받기</button>
      <button class="sub-btn">최근 추천 보기</button>
    </div>
  </div>
</template>

<script setup>
// 로직 필요하면 여기에
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #6faf9b 60%, #fff 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
}
.mascot-box {
  background: #fff;
  border-radius: 32px;
  box-shadow: 0 6px 24px 0 #21d59b30;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 88px;
  height: 88px;
  margin-bottom: 24px;
}
.mascot-face {
  font-size: 48px;
  line-height: 1;
}
.title {
  font-size: 2.4rem;
  font-weight: bold;
  color: #fff;
  margin-bottom: 8px;
  letter-spacing: 2px;
  text-shadow: 0 2px 8px #15b38240;
}
.subtitle {
  font-size: 1.2rem;
  color: #fff;
  margin-bottom: 32px;
  text-align: center;
}
.point {
  background: #fff3;
  padding: 2px 8px;
  border-radius: 12px;
  color: #ffc83d;
  font-weight: bold;
}
.button-group {
  display: flex;
  gap: 16px;
}
.main-btn,
.sub-btn {
  padding: 12px 32px;
  font-size: 1rem;
  border-radius: 20px;
  border: none;
  outline: none;
  box-shadow: 0 2px 12px #21d59b20;
  cursor: pointer;
  font-weight: 600;
  transition: 0.15s;
}
.main-btn {
  background: #fff;
  color: #21d59b;
}
.main-btn:hover {
  background: #ffc83d;
  color: #fff;
}
.sub-btn {
  background: #ffc83d;
  color: #fff;
}
.sub-btn:hover {
  background: #fff;
  color: #21d59b;
}
@media (max-width: 600px) {
  .title {
    font-size: 1.6rem;
  }
  .mascot-box {
    width: 66px;
    height: 66px;
  }
  .mascot-face {
    font-size: 36px;
  }
}
</style>
