<template>
    <div class="view-wrapper">
        <Navbar />

        <div class="container">
            <div class="row mb-4">
                <div class="col-12 text-center">
                    <h2 class="fw-bold text-primary">Panel de Métricas</h2>
                    <p class="text-semi-muted">
                        Visualización en tiempo real de los datos procesados por
                        la API
                        <span class="badge bg-warning text-dark ms-2"
                            >Version Beta</span
                        >
                    </p>
                </div>
            </div>

            <div class="row justify-content-center">
                <div class="col-md-8 col-lg-6">
                    <div v-if="isLoading" class="text-center py-5">
                        <div
                            class="spinner-border text-primary"
                            role="status"
                        ></div>
                        <p class="mt-2 text-muted">Cargando datos...</p>
                    </div>

                    <ChartPanel
                        v-else-if="backendStats"
                        :stats="backendStats.sentimentDistribution"
                    />

                    <div v-else class="alert alert-secondary text-center mt-4">
                        No hay datos para mostrar.
                    </div>
                </div>
            </div>

            <div class="text-center mt-4">
                <button
                    @click="fetchStats"
                    class="btn btn-outline-secondary btn-sm"
                >
                    <i class="bi bi-arrow-clockwise"></i> Actualizar Datos
                </button>
            </div>
        </div>
    </div>
</template>
<script setup>
import { ref, onMounted } from "vue";
import Navbar from "../components/AppNavbar.vue";
import ChartPanel from "../components/ChartPanel.vue";
import SentimentService from "../services/SentimentService";

const backendStats = ref(null);
const isLoading = ref(true);

const fetchStats = async () => {
    isLoading.value = true;
    try {
        // Pequeño delay artificial si es muy rápido para que se note la recarga (opcional)
        // await new Promise(r => setTimeout(r, 500));
        const stats = await SentimentService.getStats();
        backendStats.value = stats;
    } catch (e) {
        console.error("Error cargando stats:", e);
    } finally {
        isLoading.value = false;
    }
};

onMounted(() => {
    fetchStats();
});
</script>

<style scoped>
/* No necesitamos definir background-color ni fonts aquí.
   main.css ya maneja el body y la tipografía global.
*/

.view-wrapper {
    min-height: 100vh;
    padding-top: 140px;
    padding-bottom: 2rem;
}

/* Ajuste responsive para móviles donde el navbar no flota tanto */
@media (max-width: 991px) {
    .view-wrapper {
        padding-top: 100px;
    }
}

.section-title {
    /* Gradiente Cesium para el título */
    background: linear-gradient(to right, #fff, var(--cesium-cyan));
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
}

/* Animación simple para el icono de recarga */
.spin-icon {
    animation: spin 1s linear infinite;
}

@keyframes spin {
    100% {
        transform: rotate(360deg);
    }
}

.text-semi-muted {
    color: rgba(255, 255, 255, 0.3);
}
</style>
