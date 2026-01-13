<template>
    <div class="page-container">
        <div class="container py-4 d-flex flex-column" style="min-height: 80vh">
            <div class="row mb-5 align-items-center">
                <div class="col-lg-7 mb-4 mb-lg-0">
                    <div class="d-flex align-items-center flex-wrap gap-3">
                        <h2 class="fw-bold m-0 text-gradient-primary lh-1">
                            Panel de Métricas
                        </h2>

                        <span
                            class="badge bg-warning text-dark rounded-pill shadow-sm"
                        >
                            Beta
                        </span>

                        <div
                            class="vr bg-white opacity-25 d-none d-md-block mx-1"
                            style="height: 1.5rem"
                        ></div>

                        <p class="text-white-50 m-0 small">Tiempo Real</p>
                    </div>
                </div>

                <div class="col-lg-5 d-flex justify-content-lg-end">
                    <StatsToolbar
                        :loading="isLoading"
                        :total="backendStats?.totalAnalyzed"
                        @refresh="fetchStats"
                    />
                </div>
            </div>

            <Transition name="fade-slide" mode="out-in">
                <div
                    v-if="isLoading"
                    key="loading"
                    class="flex-grow-1 d-flex flex-column justify-content-center align-items-center"
                >
                    <div
                        class="spinner-border text-primary spinner-lg"
                        role="status"
                    ></div>
                    <p class="mt-3 text-white-50 small">
                        Sincronizando métricas...
                    </p>
                </div>

                <div v-else-if="backendStats" key="data" class="flex-grow-1">
                    <div class="row g-4 align-items-stretch">
                        <div class="col-lg-6">
                            <ChartPanel
                                :stats="backendStats.sentimentDistribution"
                            />
                        </div>
                        <div class="col-lg-6">
                            <KeywordsChart
                                :keywords="backendStats.topKeywords"
                            />
                        </div>
                    </div>
                </div>

                <div
                    v-else
                    key="empty"
                    class="flex-grow-1 d-flex justify-content-center align-items-center"
                >
                    <div class="alert surface-card border-0 text-center p-5">
                        <i
                            class="bi bi-bar-chart fs-1 text-white-50 mb-3 d-block opacity-50"
                        ></i>
                        <h5 class="text-white fw-bold">
                            Sin datos disponibles
                        </h5>
                        <p class="text-white-50 mb-4">
                            Analiza textos para generar estadísticas.
                        </p>
                        <button
                            @click="fetchStats"
                            class="btn btn-outline-light btn-sm rounded-pill px-4"
                        >
                            Reintentar
                        </button>
                    </div>
                </div>
            </Transition>
        </div>

        <ToastNotification
            v-if="toast.show"
            :show="toast.show"
            :message="toast.message"
            :type="toast.type"
            @close="toast.show = false"
        />
    </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import ChartPanel from "../components/ChartPanel.vue";
import KeywordsChart from "../components/KeywordsChart.vue";
import ToastNotification from "../components/ToastNotification.vue";
import StatsToolbar from "../components/StatsToolbar.vue";
import SentimentService from "../services/SentimentService";

const backendStats = ref(null);
const isLoading = ref(true);
const toast = ref({ show: false, message: "", type: "error" });

const fetchStats = async () => {
    isLoading.value = true;
    try {
        backendStats.value = await SentimentService.getStats();
    } catch (e) {
        const errorMessage =
            e.response?.data?.message || e.message || "Error desconocido.";

        toast.value = {
            show: true,
            message: errorMessage,
            type: "error",
        };
    } finally {
        setTimeout(() => (isLoading.value = false), 400);
    }
};

onMounted(() => fetchStats());
</script>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
    transition: all 0.3s ease;
}
.fade-slide-enter-from {
    opacity: 0;
    transform: translateY(10px);
}
.fade-slide-leave-to {
    opacity: 0;
    transform: translateY(-10px);
}
</style>
