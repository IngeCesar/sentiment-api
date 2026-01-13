<template>
    <div
        class="surface-card rounded-pill d-inline-flex align-items-center p-1 ps-2 shadow-sm"
    >
        <button
            class="btn btn-link text-decoration-none text-white p-0 d-flex align-items-center gap-2 pe-3 py-2"
            :class="{ 'opacity-50 pe-none': loading }"
            @click="$emit('refresh')"
            style="border: none"
        >
            <i
                class="bi fs-5 text-primary"
                :class="
                    loading ? 'bi-arrow-repeat spin-fast' : 'bi-arrow-clockwise'
                "
            ></i>
            <span class="fw-bold small">Actualizar</span>
        </button>

        <div v-if="total !== null" class="vr bg-white opacity-10 my-1"></div>

        <div
            v-if="total !== null"
            class="d-flex align-items-center gap-3 ps-3 pe-3 animate-fade-in"
        >
            <div class="text-end">
                <span
                    class="d-block text-white-50 xsmall fw-bold text-uppercase ls-1"
                >
                    Total
                </span>
                <span class="d-block fw-bold text-white lh-1">
                    {{ total }}
                </span>
            </div>
            <i class="bi bi-layers text-primary opacity-50 fs-5"></i>
        </div>
    </div>
</template>

<script setup>
defineProps({
    loading: Boolean,
    total: { type: [Number, String], default: null },
});

defineEmits(["refresh"]);
</script>

<style scoped>
.spin-fast {
    animation: spin 0.8s linear infinite;
}

.xsmall {
    font-size: 0.65rem;
}

.ls-1 {
    letter-spacing: 1px;
}

/* Animación suave cuando aparece el dato */
.animate-fade-in {
    animation: fadeIn 0.4s ease-out;
}

@keyframes spin {
    100% {
        transform: rotate(360deg);
    }
}
@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateX(-5px);
    }
    to {
        opacity: 1;
        transform: translateX(0);
    }
}
</style>
