<template>
    <div class="card surface-card h-100">
        <div
            class="card-body p-4 d-flex flex-column justify-content-center align-items-center text-center"
        >
            <div
                class="spinner-border text-primary spinner-lg mb-4"
                role="status"
            >
                <span class="visually-hidden">Cargando...</span>
            </div>

            <h5 class="fw-bold text-white animate-pulse">Procesando Texto</h5>

            <p class="text-muted small mb-0">
                Analizando sentimientos y patrones...
            </p>
        </div>
    </div>
</template>

<style scoped>
.animate-pulse {
    animation: pulse 1.5s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
    0%,
    100% {
        opacity: 1;
    }
    50% {
        opacity: 0.5;
    }
}
</style>
