<template>
    <div class="chart-wrapper surface-card h-100 p-4">
        <h5 class="text-center mb-4 fw-bold text-light">
            Distribución de Sentimientos
        </h5>

        <div v-if="chartData" class="chart-container">
            <Doughnut :data="chartData" :options="chartOptions" />
        </div>

        <div v-else class="text-center text-muted py-5">
            <div
                class="spinner-border spinner-border-sm mb-2"
                role="status"
            ></div>
            <p class="small m-0">Procesando gráfico...</p>
        </div>
    </div>
</template>

<script setup>
import { computed } from "vue";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";
import { Doughnut } from "vue-chartjs";

ChartJS.register(ArcElement, Tooltip, Legend);

const props = defineProps({
    stats: {
        type: Object,
        default: () => ({}),
    },
});

// Colores sincronizados con tu main.css (Cesium Palette)
const COLORS = {
    // POSITIVE -> --cesium-green (#a5fa6d)
    POSITIVE: "#a5fa6d",
    POSITIVO: "#a5fa6d",
    // NEGATIVE -> --cesium-danger (#ff5f85)
    NEGATIVE: "#ff5f85",
    NEGATIVO: "#ff5f85",
    // NEUTRAL -> --cesium-cyan (#53d9ff) o Info
    NEUTRAL: "#53d9ff",
    NEUTRO: "#53d9ff",
};

const LABELS_MAP = {
    POSITIVE: "Positivo",
    POSITIVO: "Positivo",
    NEGATIVE: "Negativo",
    NEGATIVO: "Negativo",
    NEUTRAL: "Neutro",
    NEUTRO: "Neutro",
};

const chartData = computed(() => {
    if (!props.stats || Object.keys(props.stats).length === 0) return null;

    // Extraemos las llaves una sola vez para iterar sobre ellas
    const keys = Object.keys(props.stats);
    const values = Object.values(props.stats);

    // 1. Areglo de Labels: Forzamos mayúsculas al buscar en el mapa
    const labels = keys.map((k) => LABELS_MAP[k.toUpperCase()] || k);

    // 2. Arreglo de Colores: Forzamos mayúsculas al buscar el color
    const backgroundColors = keys.map(
        (k) => COLORS[k.toUpperCase()] || "#b185ff"
    );

    return {
        labels: labels,
        datasets: [
            {
                backgroundColor: backgroundColors,
                data: values,
                borderWidth: 0,
                hoverOffset: 10,
            },
        ],
    };
});

const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
        legend: {
            position: "bottom",
            labels: {
                usePointStyle: true,
                padding: 20,
                color: "#ffffff", // IMPORTANTE: Texto blanco para Dark Mode
                font: {
                    family: "'Roboto', sans-serif", // Tu fuente global
                    size: 12,
                },
            },
        },
        tooltip: {
            // Fondo oscuro sólido para tooltips
            backgroundColor: "rgba(15, 17, 21, 0.95)",
            titleColor: "#fff",
            bodyColor: "#fff",
            borderColor: "rgba(255,255,255,0.1)",
            borderWidth: 1,
            padding: 12,
            cornerRadius: 8,
        },
    },
};
</script>

<style scoped>
/* Solo definimos altura y posición, el estilo visual viene de main.css (.surface-card) */
.chart-container {
    position: relative;
    height: 300px;
    width: 100%;
}
</style>
