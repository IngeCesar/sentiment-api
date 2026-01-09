<template>
    <div class="card surface-card h-100">
        <div class="card-body p-4 d-flex flex-column">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <label class="text-cyan small fw-bold tracking-wide">
                    <i class="fas fa-keyboard me-2"></i>ENTRADA DE DATOS
                </label>
                <span
                    class="badge bg-dark text-secondary border border-secondary border-opacity-25"
                >
                    v1.0
                </span>
            </div>

            <textarea
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
                    :disabled="isLoading || !modelValue.trim()"
                >
                    <span v-if="isLoading">
                        <span
                            class="spinner-border spinner-border-sm me-2"
                        ></span>
                        Procesando...
                    </span>
                    <span v-else>
                        Ejecutar Análisis
                        <i class="fas fa-arrow-right ms-2"></i>
                    </span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
defineProps({
    modelValue: String,
    isLoading: Boolean,
});

defineEmits(["update:modelValue", "analyze"]);
</script>
