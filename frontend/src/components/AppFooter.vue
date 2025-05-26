<template>
  <footer class="app-footer">
    <div class="footer-content">
      <div class="footer-main">
        <span class="logo">🍚 한끼위키</span>
        <nav class="footer-nav">
          <a href="/" class="footer-link">🏠 Home</a>
          <a href="/about" class="footer-link">ℹ️ About</a>
          <a href="/contact" class="footer-link">✉️ Contact</a>
        </nav>
      </div>
      <p class="copyright">© 2025 한끼위키. All rights reserved.</p>
    </div>
  </footer>
</template>

<script setup></script>

<style scoped>
.app-footer {
  background: linear-gradient(90deg, #e0fcfa 0%, #fffbe6 100%);
  padding: 1.5rem 0 1rem 0;
  border-top: 1.5px solid #b6e4e0;
  box-shadow: 0 -4px 24px 0 #7ee0d450;
}

.footer-content {
  max-width: 1100px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.4rem;
}

.footer-main {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
}

.logo {
  font-weight: 700;
  font-size: 1.25rem;
  letter-spacing: 0.5px;
  color: #23b49a;
  display: flex;
  align-items: center;
  gap: 4px;
  background: #fff9;
  padding: 4px 14px;
  border-radius: 20px;
  box-shadow: 0 1px 10px #21d59b18;
}

.footer-nav {
  display: flex;
  gap: 10px;
}

.footer-link {
  font-weight: 600;
  font-size: 1rem;
  background: #fff;
  color: #189c88;
  border-radius: 16px;
  padding: 5px 15px;
  text-decoration: none;
  transition: background 0.17s, color 0.15s, transform 0.12s;
  box-shadow: 0 1px 6px #21d59b0c;
  display: flex;
  align-items: center;
  gap: 5px;
}
.footer-link:hover {
  background: #21d59b;
  color: #fff;
  transform: translateY(-2px) scale(1.06);
  box-shadow: 0 3px 14px #21d59b30;
  text-decoration: none;
}

.copyright {
  color: #80aaa2;
  font-size: 0.97rem;
  margin-top: 0.4rem;
  letter-spacing: 0.4px;
}

@media (max-width: 700px) {
  .footer-main {
    flex-direction: column;
    gap: 10px;
  }
  .footer-link {
    font-size: 0.97rem;
    padding: 7px 13px;
  }
}
</style>
