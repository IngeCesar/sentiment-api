<template>
    <div class="dashboard-container d-flex flex-column justify-content-center">
        <Navbar :showDemoBtn="false" />

        <div class="container">
            <div class="row g-4 justify-content-center">
                <div class="col-lg-6">
                    <AnalysisInput
                        v-model="inputText"
                        :isLoading="isLoading"
                        @analyze="analyzeText"
                    />

                    <ErrorToast
                        v-if="showErrorToast"
                        :show="showErrorToast"
                        :message="errorMessage"
                        @close="showErrorToast = false"
                    />
                </div>

                <div class="col-lg-6">
                    <transition name="fade-slide" mode="out-in">
                        <div v-if="isLoading" key="loading" class="h-100">
                            <LoadingCard />
                        </div>

                        <div v-else-if="result" key="result" class="h-100">
                            <ResultCard
                                class="h-100 surface-card"
                                :sentiment="result.sentiment"
                                :confidence="result.confidence"
                                :keywords="result.keywords"
                            />
                        </div>

                        <div v-else key="empty" class="h-100">
                            <EmptyCard />
                        </div>
                    </transition>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, watch } from "vue";
import Navbar from "../components/AppNavbar.vue";
import AnalysisInput from "../components/AnalysisInput.vue";
import ResultCard from "../components/ResultCard.vue";
import LoadingCard from "../components/LoadingCard.vue";
import EmptyCard from "../components/EmptyCard.vue";
import SentimentService from "../services/SentimentService";
import ErrorToast from "../components/ErrorToast.vue";

const inputText = ref("");
const isLoading = ref(false);
const result = ref(null);

const showErrorToast = ref(false);
const errorMessage = ref("");

watch(inputText, () => {
    if (result.value) result.value = null;
    if (showErrorToast.value) showErrorToast.value = false;
});

const analyzeText = async () => {
    if (!inputText.value.trim()) return;

    isLoading.value = true;
    result.value = null;
    showErrorToast.value = false;

    try {
        result.value = await SentimentService.analyze(inputText.value);
    } catch (error) {
        errorMessage.value = error.message || "Error de conexión.";
        showErrorToast.value = true;
    } finally {
        isLoading.value = false;
    }
};
</script>

<style scoped>
.dashboard-container {
    min-height: 100vh;
    padding-top: 100px;
    padding-bottom: 2rem;
    overflow-x: hidden;
    @media (max-width: 991px) {
        padding-top: 120px;
    }
}

.fade-slide-enter-active,
.fade-slide-leave-active {
    transition: all 0.4s cubic-bezier(0.25, 1, 0.5, 1);
}
.fade-slide-enter-from {
    opacity: 0;
    transform: translateX(30px);
}
.fade-slide-leave-to {
    opacity: 0;
    transform: translateX(-30px);
}
</style>
