<template>
    <div class="chart-wrapper surface-card h-100 p-4">
        <h5 class="text-center mb-4 fw-bold text-light">
            Distribución de Sentimientos
        </h5>

        <div
            v-if="hasData"
            class="chart-container position-relative w-100 animate-fade-in"
        >
            <Doughnut :data="chartData" :options="chartOptions" />
        </div>

        <div
            v-else
            class="text-center text-muted h-75 d-flex flex-column justify-content-center"
        >
            <i class="bi bi-pie-chart fs-1 mb-3 opacity-50"></i>
            <p class="small m-0">No hay datos de distribución.</p>
        </div>
    </div>
</template>

<script setup>
import { computed } from "vue";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";
import { Doughnut } from "vue-chartjs";

ChartJS.register(ArcElement, Tooltip, Legend);

const props = defineProps({
    stats: { type: Object, default: () => ({}) },
});

/* --- Theming --- */

/* Chart.js requiere HEX/RGB explícitos, no soporta var(--css) nativamente.
   Valores espejo de main.css */
const CESIUM_PALETTE = {
    cyan: "#53d9ff",
    green: "#a5fa6d",
    danger: "#ff5f85",
    info: "#b185ff",
    surface: "rgba(30, 33, 40, 0.95)", // ajustado para tooltip
    text: "#ffffff",
};

const LABELS_MAP = {
    POSITIVE: "Positivo",
    POSITIVO: "Positivo",
    NEGATIVE: "Negativo",
    NEGATIVO: "Negativo",
    NEUTRAL: "Neutro",
    NEUTRO: "Neutro",
};

// --- Lógica de Datos ---

const hasData = computed(
    () => props.stats && Object.keys(props.stats).length > 0
);

// Helper inteligente para asignar colores basado en la clave (key)
const getColor = (key) => {
    const k = key.toUpperCase();
    if (k.includes("POS")) return CESIUM_PALETTE.green;
    if (k.includes("NEG")) return CESIUM_PALETTE.danger;
    if (k.includes("NEU")) return CESIUM_PALETTE.cyan;
    return CESIUM_PALETTE.info;
};

const chartData = computed(() => {
    if (!hasData.value) return { labels: [], datasets: [] };

    const keys = Object.keys(props.stats);
    const values = Object.values(props.stats);

    return {
        labels: keys.map((k) => LABELS_MAP[k.toUpperCase()] || k),
        datasets: [
            {
                data: values,
                borderWidth: 0,
                hoverOffset: 15, // Efecto "Pop" al pasar el mouse
                backgroundColor: keys.map((k) => getColor(k)),
            },
        ],
    };
});

// --- Configuración del Gráfico ---

const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: "70%", // Dona más fina (Modern UI)
    plugins: {
        legend: {
            position: "bottom",
            labels: {
                usePointStyle: true,
                padding: 20,
                color: CESIUM_PALETTE.text,
                font: { family: "'Roboto', sans-serif", size: 12 },
            },
        },
        tooltip: {
            backgroundColor: CESIUM_PALETTE.surface,
            titleColor: CESIUM_PALETTE.text,
            bodyColor: CESIUM_PALETTE.text,
            borderColor: "rgba(83, 217, 255, 0.2)", // Borde cyan sutil
            borderWidth: 1,
            padding: 12,
            cornerRadius: 8,
            callbacks: {
                label: (ctx) => ` ${ctx.label}: ${ctx.raw}`,
            },
        },
    },
};
</script>

<style scoped>
.chart-container {
    height: 300px;
}

/* Animación sutil de entrada */
.animate-fade-in {
    animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: scale(0.95);
    }
    to {
        opacity: 1;
        transform: scale(1);
    }
}
</style>
