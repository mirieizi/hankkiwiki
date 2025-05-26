<template>
  <div class="nutrition-chart-horizontal">
    <Bar :data="chartData" :options="chartOptions" />
  </div>
</template>

<script setup>
import { Bar } from "vue-chartjs";
import { Chart, BarElement, CategoryScale, LinearScale, Tooltip, Legend } from "chart.js";

Chart.register(BarElement, CategoryScale, LinearScale, Tooltip, Legend);

const nutritionStats = {
  kcal: { avg: 169.52 },
  carbohydrate: { avg: 23.62 },
  protein: { avg: 6.77 },
  fat: { avg: 5.45 },
  sugar: { avg: 8.51 },
  sodium: { avg: 279.06 },
  cholesterol: { avg: 16.42 },
};

const props = defineProps({
  food: { type: Object, required: true },
});

const labels = ["칼로리", "탄수화물", "단백질", "지방", "당류", "나트륨", "콜레스테롤"];
const keys = ["kcal", "carbohydrate", "protein", "fat", "sugar", "sodium", "cholesterol"];

const chartData = {
  labels,
  datasets: [
    {
      label: "보통 음식들의 평균",
      data: keys.map((k) => nutritionStats[k].avg),
      backgroundColor: "rgba(120, 200, 220, 0.23)",
      borderRadius: 14,
      barThickness: 22,
      categoryPercentage: 0.4, // ★ 변수(카테고리) 간격 넓게
      barPercentage: 0.7, // 바 두께 적당하게
    },
    {
      label: "내 음식",
      data: keys.map((k) => props.food[k] ?? 0),
      backgroundColor: "rgba(33,213,155,0.82)",
      borderColor: "#17cfa6",
      borderWidth: 2,
      borderRadius: 14,
      barThickness: 20,
      categoryPercentage: 0.4,
      barPercentage: 0.7,
    },
  ],
};

const chartOptions = {
  indexAxis: "y", // 가로형
  responsive: true,
  maintainAspectRatio: false, // 세로 길이 제어 위함!
  plugins: {
    legend: {
      display: true,
      position: "top",
      labels: { font: { size: 14 } },
    },
    tooltip: { enabled: true },
  },
  scales: {
    x: {
      beginAtZero: true,
      grid: { color: "#eee" },
      ticks: { font: { size: 13 } },
    },
    y: {
      grid: { display: false },
      ticks: { font: { size: 15 }, color: "#333" },
    },
  },
};
</script>

<style scoped>
.nutrition-chart-horizontal {
  margin: 38px 0 0 0;
  padding: 0 8px 10px 8px;
  width: 100%;
  height: 440px; /* ★★★ 세로 길이 크게 */
  display: flex;
  justify-content: center;
}
</style>
