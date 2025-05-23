<template>
  <div class="ai-animation">
    <div class="chat-window">
      <div class="chat-header">한끼위키</div>
      <div class="chat-body">
        <div v-for="(msg, idx) in displayedMessages" :key="idx" :class="['chat-bubble', msg.sender]">
          {{ msg.text }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "AIAnimation",
  data() {
    return {
      fullMessages: [
        { sender: "user", text: "오늘 뭐 먹을까" },
        { sender: "user", text: "뭔가 그동안 안 먹었던 걸 먹고 싶어." },
        { sender: "user", text: "3일동안 내가 안 먹었던것 확인해서 추천해줄래?" },
        { sender: "bot", text: "어떤 메뉴를 정할지 고민이시군요!" },
        { sender: "bot", text: "지난 3일간의 메뉴를 확인하는 중이에요." },
        { sender: "bot", text: ".... (지난 3일간 메뉴 확인 중) ..." },
      ],
      displayedMessages: [],
      intervalId: null,
    };
  },
  mounted() {
    this.animateMessages();
  },
  methods: {
    animateMessages() {
      let idx = 0;
      this.intervalId = setInterval(() => {
        if (idx < this.fullMessages.length) {
          this.displayedMessages.push(this.fullMessages[idx]);
          idx++;
        } else {
          clearInterval(this.intervalId);
          this.$emit("done");
        }
      }, 1000);
    },
  },
};
</script>

<style scoped>
.ai-animation {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.chat-window {
  width: 100%;
  height: 100%;
  background: #0b193f;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.chat-header {
  background: #1d2951;
  color: #fff;
  padding: 12px;
  font-weight: bold;
  text-align: center;
}
.chat-body {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
}
.chat-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 16px;
  color: #fff;
  animation: fadeIn 0.5s ease-out;
}
.chat-bubble.user {
  align-self: flex-start;
  background: #57b4b4;
}
.chat-bubble.bot {
  align-self: flex-end;
  background: #ffffff;
  color: #0b193f;
}
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
