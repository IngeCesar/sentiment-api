<template>
    <nav
        class="navbar navbar-expand-lg navbar-dark fixed-top py-3 custom-navbar"
    >
        <div class="container">
            <router-link class="navbar-brand d-flex align-items-center" to="/">
                <img src="/favicon.svg" alt="Logo" class="me-2 logo-brand" />
                <span class="fw-bold d-none d-sm-block brand-text">
                    CesiumFlow
                </span>
            </router-link>

            <button
                class="navbar-toggler border-0"
                type="button"
                data-bs-toggle="collapse"
                data-bs-target="#navbarNav"
            >
                <span class="navbar-toggler-icon"></span>
            </button>

            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav ms-auto align-items-center">
                    <!-- Implementación temporal -->
                    <li class="nav-item ms-3">
                        <router-link
                            to="/stats"
                            class="nav-link"
                            active-class="active text-primary fw-bold"
                        >
                            <span class="d-inline-flex align-items-center">
                                Estadísticas

                                <span
                                    class="badge rounded-pill bg-danger badge-beta ms-1"
                                    style="transform: translateY(-8px)"
                                >
                                    BETA
                                </span>
                            </span>
                        </router-link>
                    </li>

                    <li class="nav-item">
                        <a
                            class="nav-link"
                            href="https://github.com/cesiumflow/"
                            target="_blank"
                        >
                            <i class="fab fa-github me-1"></i> Github
                        </a>
                    </li>

                    <li class="nav-item ms-lg-3" v-if="showDemoBtn">
                        <router-link
                            to="/dashboard"
                            class="btn btn-outline-primary btn-sm px-4"
                        >
                            <i class="fas fa-rocket me-1"></i> Probar Demo
                        </router-link>
                    </li>
                </ul>
            </div>
        </div>
    </nav>
</template>

<script setup>
defineProps({
    showDemoBtn: {
        type: Boolean,
        default: true,
    },
});
</script>

<style scoped>
.custom-navbar {
    background-color: rgba(15, 17, 21, 0.6) !important;
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);

    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
}

@media (min-width: 992px) {
    .custom-navbar {
        top: 20px;
        left: 50%;
        transform: translateX(-50%);
        width: 90%;
        max-width: 1100px;
        border-radius: 16px;
        border: 1px solid rgba(255, 255, 255, 0.08);
        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
    }
}

.logo-brand {
    height: 40px;
    width: auto;
    border-radius: var(--radius);
    transition: transform 0.3s ease;
}

.navbar-brand:hover .logo-brand {
    transform: scale(1.2);
}

.brand-text {
    font-size: 1.25rem;
    font-weight: 700;
    letter-spacing: -1px;

    background: linear-gradient(to right, #fff, var(--cesium-cyan));
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;

    font-kerning: normal;
}

.nav-link {
    transition: color 0.3s ease, text-shadow 0.3s ease;
    font-size: 0.95rem;
    font-weight: 500;
}

.nav-link:hover {
    color: var(--cesium-cyan) !important;

    text-shadow: 0 0 12px rgba(83, 217, 255, 0.5);
}

.nav-link:hover i {
    color: var(--cesium-cyan);
    transition: color 0.3s ease;
}

/* Implementación temporal, debe cambiar lógica de backend */
.badge-beta {
    font-size: 0.55rem;
    padding: 0.35em 0.6em;
    font-weight: 600;
    letter-spacing: 0.5px;
    box-shadow: 0 2px 5px rgba(220, 53, 69, 0.4);
}

@media (max-width: 991px) {
    .badge-beta {
        font-size: 0.5rem;
    }
}
</style>
