<!--하단 바-->
<template>
  <footer class="app-footer">
    <div class="footer-content">
      <p>© 2025 한끼위키. All rights reserved</p>
      <nav class="footer-nav">
        <a href="/">Home</a>
        <a href="/about">About</a>
        <a href="/contact">Contact</a>
      </nav>
    </div>
  </footer>
</template>

<script setup></script>

<style scoped>
.app-footer {
  background: #f5f5f5;
  padding: 1rem 0;
  text-align: center;
  border-top: 1px solid #e0e0e0;
}
.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}
.footer-nav a {
  margin: 0 0.5rem;
  color: #555;
  text-decoration: none;
}
.footer-nav a:hover {
  text-decoration: underline;
}
</style>
