<template>
    <div
        v-if="show"
        class="toast-container position-fixed top-0 end-0 p-4"
        style="z-index: 2000"
    >
        <div
            class="toast show align-items-center text-bg-danger border-0"
            role="alert"
        >
            <div class="d-flex">
                <div class="toast-body">
                    <i class="fas fa-exclamation-circle me-2"></i> {{ message }}
                </div>
                <button
                    type="button"
                    class="btn-close btn-close-white me-2 m-auto"
                    @click="$emit('close')"
                ></button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { onMounted, onUnmounted } from "vue";

const props = defineProps({
    show: Boolean,
    message: String,
});

const emit = defineEmits(["close"]);

let timer;
onMounted(() => {
    timer = setTimeout(() => emit("close"), 3000);
});
onUnmounted(() => clearTimeout(timer));
</script>
