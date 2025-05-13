<template>
  <div class="app-layout">
    <!-- ① 헤더 -->
    <div class="layout-header fancy-font">
      <AppHeader />
    </div>

    <!-- ② 사이드바 -->
    <div class="layout-sidebar">
      <AppSidebar />
    </div>

    <!-- ③ 메인 -->
    <div class="layout-main">
      <slot />
    </div>

    <!-- ④ 푸터 -->
    <div class="layout-footer">
      <AppFooter />
    </div>
  </div>
</template>

<script setup>
import AppHeader from "@/components/AppHeader.vue";
import AppSidebar from "@/components/AppSidebar.vue";
import AppFooter from "@/components/AppFooter.vue";
</script>

<style scoped>
.app-layout {
  /* 헤더 높이를 변수로 정의 */
  --header-height: 64px;

  display: grid;
  height: 100vh;
  overflow: hidden;

  /* grid-template-rows: 헤더 / 본문 / 푸터 */
  grid-template-rows: var(--header-height) 1fr auto;
  grid-template-columns: 240px 1fr;
  grid-template-areas:
    "header  header"
    "sidebar main"
    "footer  footer";
}

.layout-header {
  grid-area: header;
  height: var(--header-height);
  position: sticky;
  top: 0;
  z-index: 10;
  background: #fff; /* 뒤쪽 콘텐츠 가리는 용도 */
}

.layout-sidebar {
  grid-area: sidebar;
  /* 이제 사이드바는 그리드가 만든 64px 아래에서 시작하므로
     헤더에 가려지지 않습니다 */
}

.layout-main {
  grid-area: main;
  overflow-y: auto;
  margin-left: 2rem;
  padding: 1.5rem;
}

.layout-footer {
  grid-area: footer;
  position: sticky;
  bottom: 0;
  z-index: 10;
  background: #fff;
}
</style>
