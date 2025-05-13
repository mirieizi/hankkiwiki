import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "path"; // Node.js 내장 모듈

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src"),
    },
  },
});
