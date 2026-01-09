<template>
    <div class="card surface-card h-100 empty-state-border">
        <div
            class="card-body p-4 d-flex flex-column justify-content-center align-items-center text-center opacity-75"
        >
            <div class="icon-circle mb-3">
                <i class="fas fa-chart-bar fa-2x text-secondary"></i>
            </div>

            <h5 class="text-white fw-bold">Panel de Resultados</h5>

            <p class="small text-secondary">
                Presiona
                <span class="text-cyan fw-bold">Ejecutar Análisis</span>
                para visualizar métricas
            </p>
        </div>
    </div>
</template>

<style scoped>
.icon-circle {
    width: 60px;
    height: 60px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
}
.empty-state-border {
    border: 2px dashed rgba(255, 255, 255, 0.1);
    background-color: rgba(30, 33, 40, 0.6);
}
</style>
