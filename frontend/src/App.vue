<script setup>
import { RouterView } from "vue-router";
</script>

<template>
    <RouterView />
</template>

<!-- <template>
    <router-view v-slot="{ Component }">
        <transition name="page-blur" mode="out-in">
            <component :is="Component" />
        </transition>
    </router-view>
</template> -->

<style></style>
