<template>
    <nav
        class="navbar navbar-expand-lg navbar-dark fixed-top py-3 custom-navbar"
    >
        <div class="container">
            <router-link class="navbar-brand d-flex align-items-center" to="/">
                <img src="/favicon.svg" alt="Logo" class="me-2 logo-brand" />
                <span
                    class="fw-bold d-none d-sm-block fs-5 text-gradient-primary"
                >
                    CesiumFlow
                </span>
            </router-link>

            <button
                class="navbar-toggler border-0 focus-ring"
                type="button"
                data-bs-toggle="collapse"
                data-bs-target="#navbarNav"
            >
                <i class="fas fa-bars text-white fs-4"></i>
            </button>

            <div class="collapse navbar-collapse" id="navbarNav">
                <ul
                    class="navbar-nav ms-auto align-items-center gap-lg-4 gap-3 mt-3 mt-lg-0"
                >
                    <li class="nav-item">
                        <router-link
                            to="/stats"
                            class="nav-link position-relative font-medium"
                            active-class="active-link"
                        >
                            Estadísticas
                            <span
                                class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger badge-beta"
                            >
                                BETA
                            </span>
                        </router-link>
                    </li>

                    <li class="nav-item">
                        <a
                            class="nav-link d-flex align-items-center font-medium"
                            href="https://github.com/cesiumflow/"
                            target="_blank"
                        >
                            <i class="fab fa-github fs-5 me-2"></i>
                            <span class="d-lg-none d-xl-inline">Github</span>
                        </a>
                    </li>

                    <li class="nav-item" v-if="showDemoBtn">
                        <router-link
                            to="/dashboard"
                            class="btn btn-primary btn-sm px-4 shadow-glow fw-bold rounded-pill btn-hover-lift"
                        >
                            <i class="fas fa-rocket me-2"></i>Probar Demo
                        </router-link>
                    </li>
                </ul>
            </div>
        </div>
    </nav>
</template>

<script setup>
defineProps({
    showDemoBtn: { type: Boolean, default: true },
});
</script>

<style scoped>
.custom-navbar {
    background-color: rgba(15, 17, 21, 0.85);
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
}

@media (min-width: 992px) {
    .custom-navbar {
        top: 24px;
        left: 50%;
        transform: translateX(-50%);
        width: 92%;
        max-width: 1200px;
        border-radius: 50px;
        border: 1px solid rgba(255, 255, 255, 0.08);
        box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
        padding: 0.5rem 1.5rem !important;
    }
}

.logo-brand {
    height: 32px;
    width: auto;
}

.nav-link {
    color: rgba(255, 255, 255, 0.6) !important;
    transition: color 0.3s ease;
}
.nav-link:hover {
    color: #fff !important;
}

.active-link {
    color: var(--cesium-cyan) !important;
    text-shadow: 0 0 12px rgba(83, 217, 255, 0.4);
}

.font-medium {
    font-weight: 500;
    font-size: 0.95rem;
}

.badge-beta {
    font-size: 0.5rem;
    padding: 0.35em 0.5em;
    top: 5px !important;
    box-shadow: 0 0 8px rgba(220, 53, 69, 0.4);
}

.navbar-toggler:focus {
    box-shadow: none;
}

/* EFECTO DE ELEVACIÓN SUTIL */
.btn-hover-lift {
    transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.btn-hover-lift:hover {
    transform: translateY(-2px);
    /* Incrementa el glow existente un poco */
    box-shadow: 0 0 20px rgba(83, 217, 255, 0.4) !important;
}
</style>
