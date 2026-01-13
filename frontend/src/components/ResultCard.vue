<template>
    <div
        class="d-flex flex-column justify-content-center align-items-center text-center h-100 p-4 rounded-4 transition-all"
        :class="config.styleClass"
    >
        <div class="mb-3">
            <i
                :class="['fas', config.icon]"
                style="
                    font-size: 3.5rem;
                    filter: drop-shadow(0 0 15px rgba(0, 0, 0, 0.3));
                "
            ></i>
        </div>

        <h3 class="fw-bold mb-1" :class="config.textClass">
            {{ config.label }}
        </h3>

        <p class="text-white-50 small mb-4">Sentimiento Detectado</p>

        <div class="progress w-100 mb-2 custom-progress">
            <div
                class="progress-bar fw-bold"
                role="progressbar"
                :class="config.barClass"
                :style="{ width: confidence + '%' }"
                aria-valuemin="0"
                aria-valuemax="100"
            >
                {{ confidence }}%
            </div>
        </div>

        <p class="small text-white-50 mb-4">Nivel de Confianza</p>

        <div
            v-if="keywords.length"
            class="w-100 border-top border-white border-opacity-10 pt-3 mt-auto"
        >
            <p class="small text-white-50 mb-2 text-uppercase fw-bold ls-1">
                Palabras Clave
            </p>
            <div class="d-flex flex-wrap gap-2 justify-content-center">
                <span
                    v-for="(word, index) in keywords"
                    :key="index"
                    class="badge keyword-badge fw-normal text-white"
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
        styleClass: "glow-surface-success",
        textClass: "text-success",
        barClass: "bg-success text-dark",
    },
    NEGATIVO: {
        label: "Negativo",
        icon: "fa-face-frown",
        styleClass: "glow-surface-danger",
        textClass: "text-danger",
        barClass: "bg-danger text-white",
    },
    NEUTRO: {
        label: "Neutro",
        icon: "fa-face-meh",
        styleClass: "glow-surface-primary",
        textClass: "text-primary",
        barClass: "bg-primary text-dark",
    },
    DEFAULT: {
        label: "Desconocido",
        icon: "fa-question-circle",
        styleClass: "glow-surface-secondary",
        textClass: "text-secondary",
        barClass: "bg-secondary",
    },
};

const config = computed(
    () => SENTIMENT_CONFIG[props.sentiment] || SENTIMENT_CONFIG.DEFAULT
);
</script>

<style scoped>
.transition-all {
    transition: all 0.4s ease;
}

.custom-progress {
    height: 1.5rem;
    background-color: rgba(0, 0, 0, 0.4);
    border-radius: var(--radius);
}

.keyword-badge {
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.15);
    padding: 0.5em 0.8em;
}

.ls-1 {
    letter-spacing: 1px;
    font-size: 0.7rem;
}
</style>
