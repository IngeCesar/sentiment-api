<template>
    <div class="surface-card h-100 p-4 d-flex flex-column">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <label class="text-primary small fw-bold">
                <i class="fas fa-file-csv me-2"></i>CARGA MASIVA
            </label>
            <span
                class="badge bg-dark border border-secondary text-secondary opacity-75"
            >
                Beta
            </span>
        </div>

        <div class="flex-grow-1 mb-4">
            <input
                type="file"
                ref="fileInputRef"
                accept=".csv"
                class="d-none"
                @change="handleFileSelect"
            />

            <div
                class="upload-zone h-100 d-flex flex-column justify-content-center align-items-center text-center p-3"
                :class="{ 'has-file': modelValue, 'is-loading': isLoading }"
                @click="triggerFileInput"
            >
                <div v-if="!modelValue">
                    <i
                        class="fas fa-cloud-upload-alt fs-1 mb-3 text-secondary opacity-50"
                    ></i>
                    <p class="text-white small fw-bold mb-0">
                        Click para seleccionar archivo CSV
                    </p>
                </div>

                <div v-else class="animate-pop">
                    <i class="fas fa-file-csv fs-1 text-success mb-2"></i>
                    <p
                        class="text-white fw-bold small mb-0 px-2 text-truncate file-name-max"
                    >
                        {{ modelValue.name }}
                    </p>
                    <button
                        @click.stop="removeFile"
                        class="btn btn-link text-danger text-decoration-none small p-0 mt-2 opacity-75"
                        :disabled="isLoading"
                    >
                        <i class="fas fa-trash-alt me-1"></i>Quitar
                    </button>
                </div>
            </div>
        </div>

        <div class="d-grid mt-auto">
            <button
                @click="$emit('analyze')"
                class="btn btn-primary btn-lg shadow-glow fw-bold"
                :disabled="isDisabled"
            >
                <span v-if="isLoading">
                    <span class="spinner-border spinner-border-sm me-2"></span>
                    Procesando...
                </span>
                <span v-else>
                    Procesar Lote
                    <i class="fas fa-arrow-right ms-2"></i>
                </span>
            </button>
        </div>
    </div>
</template>

<script setup>
import { computed, ref } from "vue";

const props = defineProps({
    modelValue: { type: Object, default: null },
    isLoading: { type: Boolean, default: false },
});

const emit = defineEmits(["update:modelValue", "analyze", "error"]);

const fileInputRef = ref(null);
const isDisabled = computed(() => props.isLoading || !props.modelValue);

const triggerFileInput = () => {
    if (!props.isLoading) fileInputRef.value.click();
};

const handleFileSelect = (event) => {
    const file = event.target.files[0];
    if (!file) return;

    const MAX_SIZE = 0.5 * 1024 * 1024; // 500KB

    if (!file.name.endsWith(".csv")) {
        emit("error", "El archivo debe ser un formato CSV válido.");
        event.target.value = "";
        return;
    }

    if (file.size > MAX_SIZE) {
        emit("error", "El archivo es demasiado grande (Máximo 500KB).");
        event.target.value = "";
        return;
    }

    // Si todo está bien, emitimos el archivo
    emit("update:modelValue", file);
    event.target.value = "";
};

const removeFile = () => {
    if (!props.isLoading) emit("update:modelValue", null);
};
</script>

<style scoped>
/* Reutilizamos la estética de .custom-textarea para coherencia visual */
.upload-zone {
    background-color: rgba(15, 17, 21, 0.5);
    border: 1px dashed rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
}

.upload-zone:hover:not(.is-loading) {
    background-color: rgba(15, 17, 21, 0.8);
    border-color: var(--cesium-cyan);
    /* Copiamos el box-shadow exacto del focus de tu textarea */
    box-shadow: 0 0 0 4px rgba(83, 217, 255, 0.1);
}

.upload-zone.has-file {
    border-style: solid;
    border-color: rgba(var(--bs-success-rgb), 0.4);
}

.is-loading {
    cursor: not-allowed;
    opacity: 0.6;
}

.file-name-max {
    max-width: 250px;
}

.animate-pop {
    animation: pop 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes pop {
    from {
        transform: scale(0.9);
        opacity: 0;
    }
    to {
        transform: scale(1);
        opacity: 1;
    }
}
</style>
