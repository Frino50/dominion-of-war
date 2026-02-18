import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import { localStore } from "@/store/local";
import routeService from "@/services/routeService";

const viewModules = import.meta.glob("/src/views/**/*.vue");

const staticRoutes: RouteRecordRaw[] = [
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
];

const router = createRouter({
    history: createWebHistory(),
    routes: staticRoutes,
});

let dynamicRoutesLoaded = false;
const addedRouteNames = new Set<string>();

function resolveComponentPath(componentPath: string): string {
    return "/src/views/" + componentPath.trim();
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

        if (!componentLoader) {
            console.warn(`Composant introuvable: ${componentPath}`);
            return;
        }

        router.addRoute({
            path: routePath,
            name: route.name,
            component: () => componentLoader().then((m: any) => m.default ?? m),
            meta: {
                requiresAuth: route.needAuth,
            },
        });

        addedRouteNames.add(route.name);
    });

    dynamicRoutesLoaded = true;
}

function resetDynamicRoutes() {
    addedRouteNames.forEach((name) => {
        if (router.hasRoute(name)) {
            router.removeRoute(name);
        }
    });

    addedRouteNames.clear();
    dynamicRoutesLoaded = false;
}

router.beforeEach(async (to, _from, next) => {
    const isAuthenticated = !!localStore.token;

    if (isAuthenticated && !dynamicRoutesLoaded) {
        try {
            await loadDynamicRoutes();
            return next({ ...to, replace: true });
        } catch (error) {
            console.error("Erreur chargement routes dynamiques", error);
            resetDynamicRoutes();
            localStore.token = "";
            return next("/");
        }
    }

    if (to.matched.length === 0) {
        return next("/");
    }

    if (to.meta.requiresAuth && !isAuthenticated) {
        resetDynamicRoutes();
        return next("/");
    }

    next();
});

router.addRoute({
    path: "/:pathMatch(.*)*",
    redirect: "/",
});

export { loadDynamicRoutes, resetDynamicRoutes };
export default router;
