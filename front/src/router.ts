import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import { localStore } from "./store/local";
import routeService from "@/services/routeService";

const viewModules = import.meta.glob([
    "/src/views/**/*.vue",
    "@/views/**/*.vue",
]);

let dynamicRoutesLoaded = false;

const staticRoutes: Array<RouteRecordRaw> = [
    {
        path: "/",
        name: "Home",
        component: () => import("@/views/Home.vue"),
    },
    {
        path: "/login",
        name: "Login",
        component: () => import("@/views/Login.vue"),
    },
    {
        path: "/register",
        name: "Register",
        component: () => import("@/views/Register.vue"),
    },
    // {
    //     path: "/game/arena/:roomId",
    //     name: "GameArena",
    //     component: () => import("@/views/GameArena.vue"),
    //     meta: { requiresAuth: true },
    // },
];

const router = createRouter({
    history: createWebHistory(),
    routes: staticRoutes,
});

function resolveComponentPath(componentPath: string): string {
    const path = componentPath.trim();
    return "/src/views/" + path;
}
async function loadDynamicRoutes() {
    if (dynamicRoutesLoaded) return;

    try {
        const routes = await routeService.getAvailable();

        routes.forEach((route) => {
            const routePath = route.name.startsWith("/")
                ? route.name
                : `/${route.name}`;

            const componentPath = resolveComponentPath(route.componentPath);
            const componentLoader = viewModules[componentPath];

            const component = componentLoader
                ? () => componentLoader().then((m: any) => m.default ?? m)
                : () => import("@/views/Home.vue");

            router.addRoute({
                path: routePath,
                name: route.name,
                component,
                meta: { requiresAuth: route.needAuth },
            });
        });
    } catch (error) {
        console.error("Impossible de charger les routes dynamiques");
    } finally {
        dynamicRoutesLoaded = true;
    }
}
router.beforeEach(async (to, _from, next) => {
    const isAuthenticated = !!localStore.pseudo;

    // 1. CHARGEMENT DES ROUTES DYNAMIQUES
    // Si l'utilisateur est connecté mais que les routes n'ont pas encore été injectées
    if (isAuthenticated && !dynamicRoutesLoaded) {
        try {
            await loadDynamicRoutes();
            // Une fois les routes ajoutées, on interrompt la navigation actuelle
            // et on la relance pour que le routeur reconnaisse les nouveaux chemins.
            return next({ ...to, replace: true });
        } catch (error) {
            console.error("Erreur lors de l'initialisation des routes:", error);
            return next("/login");
        }
    }

    // 2. GESTION DES ROUTES INEXISTANTES (404 / Fallback)
    // Si après le chargement des routes, la destination n'existe toujours pas
    if (to.matched.length === 0) {
        return next("/");
    }

    // 3. LOGIQUE D'AUTORISATION (Garde de navigation)
    const requiresAuth = to.meta.requiresAuth;

    // Si la route demande une auth et que l'utilisateur n'est pas connecté
    if (requiresAuth && !isAuthenticated) {
        return next("/login");
    }

    // Si l'utilisateur est déjà connecté et essaie d'aller sur Login ou Register
    if (isAuthenticated && (to.path === "/login" || to.path === "/register")) {
        return next("/");
    }

    // Si tout est ok, on valide la navigation
    next();
});

router.addRoute({
    path: "/:pathMatch(.*)*",
    redirect: "/",
});

export default router;
