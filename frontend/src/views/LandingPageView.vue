<template>
    <div>
        <Navbar />

        <header class="hero-section d-flex align-items-center">
            <div class="container text-center">
                <div class="row justify-content-center">
                    <div class="col-lg-8">
                        <h1 class="display-3 fw-bold mb-4">
                            Impulsa tu feedback con <br />
                            Nuestra herramienta
                        </h1>

                        <p class="lead text-secondary mb-5 px-md-5">
                            Transformamos reseñas y comentarios en datos
                            accionables. Nuestra misión es democratizar el
                            análisis de sentimientos con IA de alta precisión y
                            arquitectura reactiva.
                        </p>

                        <div
                            class="d-grid gap-3 d-sm-flex justify-content-sm-center"
                        >
                            <router-link
                                to="/dashboard"
                                class="btn btn-primary btn-lg px-5 py-3 fw-bold shadow-glow"
                            >
                                Comenzar Demo
                            </router-link>
                        </div>
                    </div>
                </div>
            </div>
        </header>
    </div>
</template>

<script setup>
import Navbar from "../components/AppNavbar.vue";
</script>

<style scoped>
.hero-section {
    min-height: calc(100vh - 76px);
    position: relative;
    padding-top: 140px;
}
</style>
