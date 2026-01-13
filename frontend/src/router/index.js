import { createRouter, createWebHistory } from "vue-router";
// Mantenemos el Home con carga estática
import LandingPageView from "../views/LandingPageView.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: "/",
            name: "home",
            component: LandingPageView,
            meta: {
                hideDemoBtn: true,
                title: "Inicio",
            },
        },
        {
            path: "/dashboard",
            name: "dashboard",
            // Lazy Loading
            // Solo se descarga cuando entras a /dashboard
            component: () => import("../views/DashboardView.vue"),
            meta: {
                hideDemoBtn: true,
                title: "Demo Interactiva",
            },
        },
        {
            path: "/stats",
            name: "stats",
            // Lazy Loading
            component: () => import("../views/StatsView.vue"),
            meta: {
                title: "Métricas",
            },
        },
    ],
    scrollBehavior(to, from, savedPosition) {
        if (savedPosition) {
            return savedPosition;
        } else {
            return { top: 0 };
        }
    },
});

// 4. GUARD GLOBAL: Actualiza el título de la pestaña del navegador
router.beforeEach((to, from, next) => {
    const appName = "CesiumFlow";
    document.title = to.meta.title ? `${appName} | ${to.meta.title}` : appName;
    next();
});

export default router;
