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

    dynamicRoutesLoaded = true;
}

router.beforeEach(async (to, _from, next) => {
    await loadDynamicRoutes();

    // Si la route n'existe toujours pas après le chargement, rediriger vers home
    if (to.matched.length === 0) {
        next("/");
        return;
    }

    const isAuthenticated = !!localStore.pseudo;
    const requiresAuth = to.meta.requiresAuth;

    // Redirection si auth requise mais non connecté
    if (requiresAuth && !isAuthenticated) {
        next("/login");
        return;
    }

    // Redirection si déjà connecté et accès à login/register
    if ((to.path === "/login" || to.path === "/register") && isAuthenticated) {
        next("/");
        return;
    }

    next();
});

router.addRoute({
    path: "/:pathMatch(.*)*",
    redirect: "/",
});

export default router;
