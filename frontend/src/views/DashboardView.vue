<template>
    <div class="page-container">
        <div
            class="container flex-grow-1 d-flex flex-column justify-content-center py-4"
        >
            <div class="row justify-content-center mb-4">
                <div class="col-lg-6">
                    <div
                        class="surface-card p-1 d-flex rounded-pill"
                        style="border-radius: 50rem !important"
                    >
                        <button
                            @click="setMode('text')"
                            class="btn flex-fill rounded-pill fw-bold transition-all small"
                            :class="
                                mode === 'text'
                                    ? 'btn-primary shadow-glow'
                                    : 'text-white-50 hover-light'
                            "
                        >
                            <i class="fas fa-keyboard me-2"></i>Texto
                        </button>

                        <button
                            @click="setMode('batch')"
                            class="btn flex-fill rounded-pill fw-bold transition-all small"
                            :class="
                                mode === 'batch'
                                    ? 'btn-primary shadow-glow'
                                    : 'text-white-50 hover-light'
                            "
                        >
                            <i class="fas fa-file-csv me-2"></i>Lote CSV
                        </button>
                    </div>
                </div>
            </div>

            <div class="row g-4 justify-content-center align-items-stretch">
                <div class="col-lg-6">
                    <transition name="fade-slide" mode="out-in">
                        <AnalysisInput
                            v-if="mode === 'text'"
                            key="text-input"
                            v-model="inputText"
                            :isLoading="isLoading"
                            @analyze="analyzeText"
                        />

                        <BatchInput
                            v-else
                            key="batch-input"
                            v-model="batchFile"
                            :isLoading="isLoading"
                            @analyze="analyzeBatch"
                            @error="(msg) => showFeedback(msg, 'warning')"
                        />
                    </transition>
                </div>

                <div class="col-lg-6">
                    <transition name="fade-slide" mode="out-in">
                        <div v-if="isLoading" key="loading" class="h-100">
                            <LoadingCard />
                        </div>

                        <div
                            v-else-if="mode === 'text' && result"
                            key="result"
                            class="h-100"
                        >
                            <ResultCard
                                class="h-100"
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
import { ref, watch, nextTick } from "vue";
import AnalysisInput from "../components/AnalysisInput.vue";
import ResultCard from "../components/ResultCard.vue";
import LoadingCard from "../components/LoadingCard.vue";
import EmptyCard from "../components/EmptyCard.vue";
import ToastNotification from "../components/ToastNotification.vue";
import SentimentService from "../services/SentimentService";
import BatchInput from "../components/BatchInput.vue";

// --- ESTADO ---
const mode = ref("text"); // 'text' o 'batch'
const inputText = ref("");
const batchFile = ref(null);
const isLoading = ref(false);
const result = ref(null);
const toast = ref({ show: false, message: "", type: "error" });

// --- HELPERS ---
const showFeedback = async (msg, type = "error") => {
    // Desmonta el componente
    toast.value.show = false;
    // Asegura que el DOM se actualice
    await nextTick();
    // Monta uno nuevo (reinicia el timer del onMounted)
    toast.value = {
        show: true,
        message: msg,
        type: type,
    };
};

const setMode = (newMode) => {
    mode.value = newMode;
    result.value = null;
};

// Limpieza reactiva: Si el usuario escribe, borramos el resultado anterior
watch([inputText, batchFile], () => {
    if (result.value) result.value = null;
});

const analyzeText = async () => {
    const text = inputText.value.trim();

    // Validaciones Rápidas
    if (!text) return showFeedback("Escribe algo para analizar.", "warning");

    const hasLetters = /[a-zA-ZñÑáéíóúÁÉÍÓÚüÜ]/.test(text);

    if (!hasLetters) {
        return showFeedback(
            "El texto debe contener palabras, no solo números o símbolos.",
            "warning"
        );
    }

    // CARACTERES TÉCNICOS PROHIBIDOS
    // Bloqueamos < > (HTML/Scripts), { } (Objetos/Code), \ (Escapes), ` (Ejecución)
    const forbiddenChars = /[<>{}[\]\\`^]/;

    // PATRONES DE ATAQUE (La "Lista Negra")
    // Usamos límites de palabra (\b) para ser más precisos y evitar falsos positivos.
    const attackPatterns = new RegExp(
        [
            // SQL: Solo frases compuestas peligrosas
            "\\bunion\\s+select\\b", // Robo de tablas
            "\\bselect\\s+\\*\\s+from\\b", // Extracción masiva
            "\\bdrop\\s+table\\b", // Borrado
            "\\binsert\\s+into\\b", // Inserción
            "\\-\\-", // Comentario SQL
            "\\/\\*", // Comentario Bloque

            // Code Injection Genérico (Funciona para JS, Java, etc.)
            "\\bconsole\\.log\\b", // Debug JS
            "\\balert\\s*\\(", // XSS JS clásico
            "\\bsystem\\.out\\b", // Java (Opcional: Si lo quitas, reduces pistas)
            "\\beval\\s*\\(", // Ejecución dinámica
        ].join("|"),
        "i"
    );

    if (forbiddenChars.test(text)) {
        return showFeedback(
            "El texto contiene caracteres técnicos no permitidos (< > { } \\ ` ^).",
            "warning"
        );
    }

    if (attackPatterns.test(text)) {
        // Mensaje neutro
        return showFeedback("No se pudo procesar el texto.", "warning");
    }

    if (text.length < 3)
        return showFeedback(
            "El texto es muy corto (mínimo 3 caracteres).",
            "warning"
        );

    if (text.length > 5000)
        return showFeedback(
            "El texto excede el límite de 5000 caracteres.",
            "warning"
        );

    // Ejecución
    isLoading.value = true;
    result.value = null;

    try {
        result.value = await SentimentService.analyze(text);
    } catch (error) {
        // Debería atrapar el error "El motor de IA no está disponible"
        const msg =
            error.response?.data?.message ||
            error.message ||
            "Error al procesar la solicitud.";

        showFeedback(msg, "error");
    } finally {
        isLoading.value = false;
    }
};

const analyzeBatch = async () => {
    if (!batchFile.value) return;

    isLoading.value = true;
    try {
        const data = await SentimentService.uploadBatch(batchFile.value);

        showFeedback(
            `Proceso completado. Registros procesados: ${
                data.totalProcessed || 0
            }. ` +
                `Exitosos: ${data.success || 0}. Fallidos: ${data.failed || 0}`,
            "success"
        );

        batchFile.value = null; // Limpiar archivo tras éxito
    } catch (error) {
        // PRIORIDAD 1: El mensaje configurado en el interceptor o el servidor
        // PRIORIDAD 2: El mensaje nativo del objeto Error (error.message)
        // PRIORIDAD 3: Fallback genérico
        const msg =
            error.response?.data?.message ||
            error.message ||
            "Error inesperado al procesar el lote.";

        showFeedback(msg, "error");
    } finally {
        isLoading.value = false;
    }
};
</script>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
    transition: all 0.4s cubic-bezier(0.25, 1, 0.5, 1);
}

.fade-slide-enter-from {
    opacity: 0;
    transform: translateY(15px);
}

.fade-slide-leave-to {
    opacity: 0;
    transform: translateY(-15px);
}
</style>
