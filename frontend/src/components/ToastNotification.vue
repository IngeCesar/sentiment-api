<template>
    <div
        v-if="show"
        class="toast-container position-fixed top-0 end-0 p-4 toast-z-index"
    >
        <div
            class="toast show align-items-center surface-card border-0"
            :class="config.styleClass"
            role="alert"
            aria-live="assertive"
            aria-atomic="true"
        >
            <div class="d-flex">
                <div class="toast-body d-flex align-items-center w-100">
                    <i :class="['fs-4 me-3', config.icon]"></i>

                    <div class="d-flex flex-column">
                        <strong
                            v-if="config.title"
                            class="mb-1 text-uppercase ls-1"
                            style="font-size: 0.8rem"
                        >
                            {{ config.title }}
                        </strong>

                        <span class="text-white small">{{ message }}</span>
                    </div>
                </div>

                <button
                    type="button"
                    class="btn-close btn-close-white me-3 m-auto"
                    @click="$emit('close')"
                    aria-label="Close"
                ></button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from "vue";

const props = defineProps({
    show: Boolean,
    message: { type: String, required: true },
    type: { type: String, default: "error" }, // 'success' | 'warning' | 'error'
});

const emit = defineEmits(["close"]);

const TOAST_CONFIG = {
    success: {
        title: "¡Éxito!",
        icon: "bi bi-check-circle-fill",
        styleClass: "glow-surface-success",
    },
    warning: {
        title: "Advertencia",
        icon: "bi bi-exclamation-triangle-fill",
        styleClass: "glow-surface-warning",
    },
    error: {
        title: "Error",
        icon: "bi bi-x-circle-fill",
        styleClass: "glow-surface-danger",
    },
};

const config = computed(() => TOAST_CONFIG[props.type] || TOAST_CONFIG.error);

let timer;

onMounted(() => {
    timer = setTimeout(() => emit("close"), 4000);
});

onUnmounted(() => {
    if (timer) clearTimeout(timer);
});
</script>

<style scoped>
.toast-z-index {
    z-index: 2000;
}

/* Animación de entrada suave, efecto rebote*/
.toast {
    animation: slideInRight 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.ls-1 {
    letter-spacing: 1px;
}

@keyframes slideInRight {
    from {
        transform: translateX(100%);
        opacity: 0;
    }
    to {
        transform: translateX(0);
        opacity: 1;
    }
}
</style>
