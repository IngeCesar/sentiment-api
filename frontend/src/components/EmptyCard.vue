<template>
    <div class="card h-100 ghost-card">
        <div
            class="card-body p-4 d-flex flex-column justify-content-center align-items-center text-center"
        >
            <div
                class="icon-circle d-flex align-items-center justify-content-center rounded-circle mb-3 text-secondary"
            >
                <i class="fas fa-chart-bar fa-2x"></i>
            </div>

            <h5 class="text-secondary fw-bold mb-0">Panel de Resultados</h5>
        </div>
    </div>
</template>

<style scoped>
.ghost-card {
    background-color: rgba(30, 33, 40, 0.6);
    border: 2px dashed rgba(255, 255, 255, 0.1);
    border-radius: 16px;
    transition: all 0.3s ease;
    /* Evita interacción ya que es solo visual */
    user-select: none;
    pointer-events: none;
}

.icon-circle {
    width: 60px;
    height: 60px;
    background: rgba(255, 255, 255, 0.03);
}
</style>
