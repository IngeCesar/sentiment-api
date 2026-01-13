<template>
    <div class="surface-card h-100 p-4 d-flex flex-column">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <label for="analysis-text" class="text-primary small fw-bold">
                <i class="fas fa-keyboard me-2"></i>ENTRADA DE DATOS
            </label>

            <span
                class="badge bg-dark border border-secondary text-secondary opacity-75"
            >
                v1.0
            </span>
        </div>

        <textarea
            id="analysis-text"
            :value="modelValue"
            @input="$emit('update:modelValue', $event.target.value)"
            class="form-control custom-textarea mb-4 flex-grow-1"
            rows="8"
            placeholder="Escribe o pega aquí el texto para analizar..."
            :disabled="isLoading"
        ></textarea>

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
                    Ejecutar Análisis
                    <i class="fas fa-arrow-right ms-2"></i>
                </span>
            </button>
        </div>
    </div>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
    modelValue: { type: String, default: "" },
    isLoading: { type: Boolean, default: false },
});

defineEmits(["update:modelValue", "analyze"]);

// Deshabilitado si carga O si está vacío/solo espacios
const isDisabled = computed(() => {
    return props.isLoading || !props.modelValue || !props.modelValue.trim();
});
</script>

<style scoped></style>
