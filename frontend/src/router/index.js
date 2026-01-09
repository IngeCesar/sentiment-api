import { createRouter, createWebHistory } from "vue-router";

import LandingPageView from "../views/LandingPageView.vue";
import DashboardView from "../views/DashboardView.vue";
import StatsView from "../views/StatsView.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: "/",
            name: "home",
            component: LandingPageView,
        },
        {
            path: "/dashboard",
            name: "dashboard",
            component: DashboardView,
        },
        {
            path: "/stats",
            name: "stats",
            component: StatsView,
        },
    ],
});

export default router;
