<script setup>
import { computed } from "vue";
import { RouterView, useRoute } from "vue-router";
import Navbar from "./components/AppNavbar.vue";

const route = useRoute();
const showDemoBtn = computed(() => !route.meta.hideDemoBtn);
</script>

<template>
    <Navbar :showDemoBtn="showDemoBtn" />

    <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
        </transition>
    </router-view>
</template>

<style>
.fade-slide-enter-active,
.fade-slide-leave-active {
    transition: opacity 0.3s ease, transform 0.3s ease;
}

/* Estado inicial de la página que ENTRA (Empieza un poco abajo y transparente) */
.fade-slide-enter-from {
    opacity: 0;
    transform: translateY(15px);
}

/* Estado final de la página que SALE (Termina un poco arriba y transparente) */
.fade-slide-leave-to {
    opacity: 0;
    transform: translateY(-15px);
}
</style>
