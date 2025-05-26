<template>
  <div class="about-bg">
    <div class="about-grid">
      <!-- 소개 텍스트 -->
      <div class="about-text">
        <h1>👩‍💻 한끼위키 팀원 소개</h1>
        <p>
          우리 팀은 둘이서 기획부터 개발, 디자인까지 올인원으로 작업했어요!
          <br />
          아래 스마트폰에서 멤버 정보를 확인해보세요😊
        </p>
      </div>
      <!-- 스마트폰 목업 (지민, 미림) -->
      <div class="mockup-phone" v-for="member in members" :key="member.name">
        <div class="mockup-notch"></div>
        <div class="mockup-content-cardnews">
          <!-- 프로필 헤드 -->
          <div class="profile-head">
            <img :src="member.img" :alt="member.name" class="profile-img" />
            <div>
              <div class="name">{{ member.name }}</div>
              <div class="role">{{ member.role }}</div>
            </div>
          </div>
          <div class="profile-desc">{{ member.desc }}</div>
          <!-- FE/BE 카드뉴스 -->
          <div class="cardnews-cards">
            <div class="cardnews-card fe-card">
              <div class="cardnews-title">FrontEnd</div>
              <ul>
                <li v-for="w in member.fe" :key="w">✔️ {{ w }}</li>
              </ul>
            </div>
            <div class="cardnews-card be-card">
              <div class="cardnews-title">BackEnd</div>
              <ul>
                <li v-for="w in member.be" :key="w">✔️ {{ w }}</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import logo3 from "@/assets/logo3.png"; // 실제 이미지 경로에 맞게 변경!
const members = [
  {
    name: "지민",
    role: "FE / BE",
    img: logo3,
    desc: "jwt security + RAG 기능 구현",
    fe: ["웹 기본 디자인 화면 구현", "로그인, 회원가입 화면 구현", "마이페이지 화면 구현", "추천 페이지 화면 구현"],
    be: ["JWT Security 구현 (로그인, 회원가입)", "User 도메인 구현", "Diary 도메인 구현", "RAG 검색 파이프라인 구현"],
  },
  {
    name: "미림",
    role: "FE / BE",
    img: logo3,
    desc: "기록과 추천 로직 담당!",
    fe: ["캘린더 화면 구현", "식사 검색과 입력 화면 구현"],
    be: ["사용자 정보 기반 추천 / 로그 기능 구현", "KNN, vector 활용 거리 계산 기능 구현", "Food 도메인 구현", "Diet 도메인 구현", "성능 개선을 위한 DB 재설계"],
  },
];
</script>

<style scoped>
.about-bg {
  min-height: 100vh;
  width: 100vw;
  background: linear-gradient(to bottom, #f8faf8 85%, #e4f9ee 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.about-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 2.5vw;
  width: 96vw;
  max-width: 1660px;
  align-items: stretch;
  justify-items: center;
}

.about-text {
  align-self: center;
  justify-self: center;
  color: #233048;
  font-size: 1.2rem;
  width: 100%;
  max-width: 390px;
  min-width: 290px;
}
.about-text h1 {
  font-size: 2rem;
  margin-bottom: 12px;
  color: #233048;
  font-weight: 800;
  letter-spacing: -2px;
}
.about-text p {
  font-size: 1.09rem;
  line-height: 1.55;
  color: #506179;
  font-weight: 400;
}

/* --- 스마트폰 목업 --- */
.mockup-phone {
  width: 480px;
  height: 940px;
  border-radius: 46px;
  background: #fff;
  box-shadow: 0 10px 38px #21d59b18, 0 2px 12px #0a684214;
  position: relative;
  overflow: hidden;
  border: 2.7px solid #e3e9ee;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: box-shadow 0.22s;
}
.mockup-phone:hover {
  box-shadow: 0 18px 52px #20d59b2c;
}
.mockup-notch {
  width: 102px;
  height: 18px;
  background: #f2f2f2;
  border-radius: 12px;
  position: absolute;
  top: 26px;
  left: 50%;
  transform: translateX(-50%);
  opacity: 0.64;
  z-index: 2;
}
.mockup-content-cardnews {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 64px 0 24px 0; /* 위아래 여백 넓게 */
  overflow-y: auto;
  box-sizing: border-box;
  gap: 16px;
  justify-content: flex-start;
}

/* --- 프로필 --- */
.profile-head {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 10px;
  margin-top: 0;
}
.profile-img {
  width: 62px;
  height: 62px;
  border-radius: 50%;
  background: #f3f8fd;
  object-fit: cover;
  border: 2.7px solid #c0f5e3;
}
.name {
  font-size: 1.18rem;
  font-weight: 800;
  color: #15b099;
  margin-bottom: 2px;
}
.role {
  font-size: 1.01rem;
  color: #ffc83d;
  font-weight: 700;
}
.profile-desc {
  font-size: 0.98rem;
  color: #667e82;
  margin-bottom: 20px;
  margin-left: 6px;
  width: 100%;
}

/* --- 카드뉴스 --- */
.cardnews-cards {
  width: 92%;
  display: flex;
  flex-direction: column;
  gap: 36px; /* 카드 간 간격 크게 */
  margin-top: 16px;
  margin-bottom: 8px;
}
.cardnews-card {
  background: #f7fefc;
  border-radius: 28px;
  box-shadow: 0 6px 24px #18d6ae16;
  padding: 36px 38px 30px 38px; /* 더 넉넉하게 */
  width: 100%;
  min-width: 0;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.fe-card {
  border-left: 9px solid #42e1b6;
}
.be-card {
  border-left: 9px solid #437fff;
  background: #f5faff;
}
.cardnews-title {
  font-size: 1.18rem;
  font-weight: 900;
  color: #13be91;
  margin-bottom: 18px;
  letter-spacing: 1.5px;
}
.fe-card .cardnews-title {
  color: #18b79c;
}
.be-card .cardnews-title {
  color: #3e7fff;
}
.cardnews-card ul {
  margin: 0;
  padding-left: 1.15em;
  color: #249a7a;
  font-size: 1.13rem;
  line-height: 2.1;
}
.be-card ul {
  color: #4066b0;
}
.cardnews-card li {
  margin-bottom: 8px;
  font-weight: 500;
  word-break: keep-all;
}

/* --- 반응형 --- */
@media (max-width: 1400px) {
  .about-grid {
    grid-template-columns: 1fr 1.2fr 1.2fr;
    gap: 1vw;
  }
  .mockup-phone {
    width: 340px;
    height: 700px;
  }
}
@media (max-width: 1023px) {
  .about-grid {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto auto;
    gap: 22px;
    max-width: 100vw;
    width: 100vw;
    justify-items: center;
  }
  .about-text {
    max-width: 96vw;
    margin-bottom: 10px;
    font-size: 1.1rem;
    text-align: center;
  }
  .mockup-phone {
    width: 96vw;
    min-width: 300px;
    max-width: 390px;
    height: 540px;
    margin: 0 auto;
  }
}
</style>
