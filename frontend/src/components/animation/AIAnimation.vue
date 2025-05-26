<template>
  <div class="ai-animation">
    <div class="chat-window">
      <div class="chat-header">한끼위키</div>
      <div class="chat-body" ref="chatBody">
        <div v-for="(msg, idx) in displayedMessages" :key="idx" :class="['chat-bubble', msg.sender]">
          {{ msg.text }}
        </div>
      </div>
      <div v-if="inputActive" class="chat-input-row">
        <input v-model="userInput" @keydown.enter="sendUserInput" :placeholder="inputPlaceholder" />
        <button @click="sendUserInput">전송</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "AIAnimation",
  data() {
    return {
      steps: [
        { sender: "bot", text: "오늘 어떤 메뉴가 땡기세요?", input: true, placeholder: "예: 매콤한 것, 담백한 것 등" },
        { sender: "bot", text: "최근 3일간 먹은 음식 기록을 확인 중이에요...", input: false },
        { sender: "bot", text: "건강 정보도 참고해서 추천해드릴게요.", input: false },
        { sender: "bot", text: "마지막으로 특별히 피하고 싶은 음식이 있으신가요?", input: true, placeholder: "예: 해산물, 튀김 등" },
        { sender: "bot", text: "알겠습니다! 잠시만 기다려주세요.", input: false },
        { sender: "bot", text: "AI에게 응답을 요청하고 있어요", input: false },
        { sender: "bot", text: "AI가 응답을 생성하는 중입니다. 📤", input: false },
      ],
      displayedMessages: [],
      currentStep: 0,
      inputActive: false,
      userInput: "",
      inputPlaceholder: "",
      preferInput: "", // 선호
      avoidInput: "", // 비선호
      inputCount: 0, // 몇 번째 input인지 체크
    };
  },
  mounted() {
    this.nextStep();
  },
  updated() {
    this.$nextTick(() => {
      const chatBody = this.$refs.chatBody;
      if (chatBody) {
        chatBody.scrollTop = chatBody.scrollHeight;
      }
    });
  },
  methods: {
    nextStep() {
      if (this.currentStep >= this.steps.length) {
        // 모든 대화 끝나면 이벤트 발행 (prefer, avoid 같이 보냄)
        this.$emit("ai-finish", {
          prefer: this.preferInput,
          avoid: this.avoidInput,
        });
        return;
      }
      const step = this.steps[this.currentStep];
      this.displayedMessages.push({ sender: step.sender, text: step.text });
      if (step.input) {
        this.inputActive = true;
        this.inputPlaceholder = step.placeholder || "";
      } else {
        this.inputActive = false;
        setTimeout(() => {
          this.currentStep++;
          this.nextStep();
        }, 900);
      }
    },
    sendUserInput() {
      if (!this.userInput.trim()) return;
      this.displayedMessages.push({ sender: "user", text: this.userInput });

      // 입력값 저장: 1번째는 prefer, 2번째는 avoid
      if (this.inputCount === 0) {
        this.preferInput = this.userInput;
      } else if (this.inputCount === 1) {
        this.avoidInput = this.userInput;
      }
      this.inputCount++;
      this.inputActive = false;
      this.userInput = "";
      this.currentStep++;
      setTimeout(() => this.nextStep(), 300);
    },
  },
};
</script>

<style scoped>
.ai-animation {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: stretch;
  justify-content: stretch;
}

.chat-window {
  width: 100%;
  height: 100%;
  background: transparent;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
}

.chat-header {
  background: #1d2951;
  color: #fff;
  padding: 12px;
  font-weight: bold;
  text-align: center;
  flex-shrink: 0;
}

.chat-body {
  flex: 1 1 0%;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  min-height: 0;
}

.chat-bubble {
  max-width: 75%;
  padding: 12px 16px;
  border-radius: 16px;
  color: #fff;
  animation: fadeIn 0.4s;
  word-break: break-all;
}
.chat-bubble.user {
  align-self: flex-start;
  background: #3b5bac;
}
.chat-bubble.bot {
  align-self: flex-end;
  background: #fff;
  color: #0b193f;
}

.chat-input-row {
  display: flex;
  padding: 8px 12px;
  background: #f2f3fa;
  border-top: 1px solid #eaeaf0;
  flex-shrink: 0;
}
.chat-input-row input {
  flex: 1;
  border: none;
  border-radius: 12px;
  padding: 9px 13px;
  margin-right: 7px;
  background: #f2f3fa;
  color: #222;
  font-size: 1rem;
}
.chat-input-row button {
  border: none;
  border-radius: 12px;
  padding: 9px 18px;
  background: #22d7b6;
  color: #fff;
  font-weight: bold;
  cursor: pointer;
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
