<template>
    <div class="chart-wrapper surface-card h-100 p-4">
        <h5 class="text-center mb-4 fw-bold text-light">Top Palabras Clave</h5>

        <div
            v-if="hasData"
            class="chart-container position-relative w-100 animate-fade-in"
        >
            <Bar :data="chartData" :options="chartOptions" />
        </div>

        <div
            v-else
            class="text-center text-muted h-75 d-flex flex-column justify-content-center"
        >
            <i class="bi bi-chat-square-quote fs-1 mb-3 opacity-50"></i>
            <p class="small m-0">No se encontraron palabras destacadas.</p>
        </div>
    </div>
</template>

<script setup>
import { computed } from "vue";
import {
    Chart as ChartJS,
    Title,
    Tooltip,
    Legend,
    BarElement,
    CategoryScale,
    LinearScale,
} from "chart.js";
import { Bar } from "vue-chartjs";

ChartJS.register(
    Title,
    Tooltip,
    Legend,
    BarElement,
    CategoryScale,
    LinearScale
);

const props = defineProps({
    keywords: { type: Array, default: () => [] },
});

/* ---Theming --- */
const CESIUM_THEME = {
    fill: "rgba(83, 217, 255, 0.25)",
    border: "#53d9ff",
    hover: "rgba(83, 217, 255, 0.45)",
    surface: "rgba(30, 33, 40, 0.95)",
    text: "#ffffff",
    grid: "rgba(255, 255, 255, 0.05)",
};

const hasData = computed(() => props.keywords && props.keywords.length > 0);

const chartData = computed(() => {
    if (!hasData.value) return { labels: [], datasets: [] };

    return {
        labels: props.keywords.map((k) => k.keyword),
        datasets: [
            {
                label: "Frecuencia",
                data: props.keywords.map((k) => k.count),
                backgroundColor: CESIUM_THEME.fill,
                borderColor: CESIUM_THEME.border,
                borderWidth: 1,
                borderRadius: 4,
                hoverBackgroundColor: CESIUM_THEME.hover,
                barPercentage: 0.6,
                indexAxis: "y",
            },
        ],
    };
});

const chartOptions = {
    indexAxis: "y",
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
        legend: { display: false },
        tooltip: {
            // Estilo consistente con ChartPanel
            backgroundColor: CESIUM_THEME.surface,
            titleColor: CESIUM_THEME.text,
            bodyColor: CESIUM_THEME.text,
            borderColor: "rgba(83, 217, 255, 0.2)",
            borderWidth: 1,
            padding: 12,
            cornerRadius: 8,
            callbacks: {
                label: (ctx) => ` Repeticiones: ${ctx.raw}`,
            },
        },
    },
    scales: {
        x: {
            grid: {
                color: CESIUM_THEME.grid,
                borderDash: [5, 5], // Línea punteada
            },
            ticks: {
                color: "#adb5bd",
                font: { size: 10 },
            },
        },
        y: {
            grid: { display: false },
            ticks: {
                color: CESIUM_THEME.text,
                font: { weight: "600", size: 12 },
            },
        },
    },
};
</script>

<style scoped>
/* Ancho y posición van por Bootstrap. */
.chart-container {
    height: 300px;
}

.animate-fade-in {
    animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateX(-10px);
    }
    to {
        opacity: 1;
        transform: translateX(0);
    }
}
</style>
