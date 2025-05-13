import { createRouter, createWebHistory } from "vue-router";

import Home from "@/views/Home.vue";
import About from "@/views/About.vue";
import RegisterFood from "@/views/RegisterFood.vue";

const routes = [
  { path: "/", name: "Home", component: Home },
  { path: "/about", name: "About", component: About },
  { path: "/food/register", name: "RegisterFood", component: RegisterFood },
];

const router = createRouter({
  history: createWebHistory(), // HTML5 history 모드
  routes,
});
export default router;
