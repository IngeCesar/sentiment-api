<template>
    <div
        class="d-flex flex-column justify-content-center align-items-center text-center h-100 p-4"
    >
        <div class="icon-circle mb-3" :class="config.bgClass">
            <i :class="['fas', config.icon, config.textClass]"></i>
        </div>

        <h3 class="fw-bold mb-1" :class="config.textClass">
            {{ config.label }}
        </h3>
        <p class="text-secondary small mb-4">Sentimiento Detectado</p>

        <div class="progress w-100 mb-2 custom-progress">
            <div
                class="progress-bar fw-bold"
                role="progressbar"
                :class="config.barClass"
                :style="{ width: confidence + '%' }"
                :aria-valuenow="confidence"
                aria-valuemin="0"
                aria-valuemax="100"
            >
                {{ confidence }}%
            </div>
        </div>
        <p class="small text-secondary mb-4">Nivel de Confianza</p>

        <div
            v-if="keywords.length"
            class="w-100 border-top border-secondary border-opacity-25 pt-3 mt-auto"
        >
            <p class="small text-secondary mb-2 text-uppercase spacing-wide">
                Palabras Clave
            </p>

            <div class="d-flex flex-wrap gap-2 justify-content-center">
                <span
                    v-for="(word, index) in keywords"
                    :key="index"
                    class="badge keyword-badge fw-normal"
                >
                    #{{ word }}
                </span>
            </div>
        </div>
    </div>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
    sentiment: { type: String, required: true },
    confidence: { type: Number, default: 0 },
    keywords: { type: Array, default: () => [] },
});

const SENTIMENT_CONFIG = {
    POSITIVO: {
        label: "Positivo",
        icon: "fa-face-smile",
        textClass: "text-success",
        bgClass: "bg-success bg-opacity-10",
        barClass: "bg-success text-dark",
    },
    NEGATIVO: {
        label: "Negativo",
        icon: "fa-face-frown",
        textClass: "text-danger",
        bgClass: "bg-danger bg-opacity-10",
        barClass: "bg-danger text-white",
    },
    NEUTRO: {
        label: "Neutro",
        icon: "fa-face-meh",
        textClass: "text-warning",
        bgClass: "bg-warning bg-opacity-10",
        barClass: "bg-warning text-dark",
    },
    DEFAULT: {
        label: "Desconocido",
        icon: "fa-question-circle",
        textClass: "text-secondary",
        bgClass: "bg-secondary bg-opacity-10",
        barClass: "bg-secondary",
    },
};

const config = computed(
    () => SENTIMENT_CONFIG[props.sentiment] || SENTIMENT_CONFIG.DEFAULT
);
</script>

<style scoped>
.icon-circle {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 2.5rem;
    transition: all 0.3s ease;
}

.custom-progress {
    height: 1.5rem;
    background-color: rgba(255, 255, 255, 0.05);
    border-radius: var(--radius);
}

.progress-bar {
    border-radius: var(--radius);
}

.keyword-badge {
    background-color: rgba(0, 0, 0, 0.4);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: #adb5bd;
}

.spacing-wide {
    letter-spacing: 1px;
    font-size: 0.7rem;
    font-weight: 700;
}
</style>
