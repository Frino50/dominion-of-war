import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import routeService from "@/services/routeService.ts";
import RouteDto from "@/models/dtos/RouteDto.ts";

const routes: RouteRecordRaw[] = [
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
    {
        path: "/:pathMatch(.*)*",
        name: "NotFound",
        redirect: "/",
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

const viewModules = import.meta.glob("/src/views/**/*.vue");
const listRoutesNames = new Set<string>();

async function loadDynamicRoutes(): Promise<RouteDto[]> {
    const routes = await routeService.getAvailableRoutes();

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
        listRoutesNames.add(route.name);
    });
    return routes;
}

function resetDynamicRoutes() {
    listRoutesNames.forEach((name) => {
        if (router.hasRoute(name)) {
            router.removeRoute(name);
        }
    });

    listRoutesNames.clear();
}

function resolveComponentPath(componentPath: string): string {
    return "/src/views/" + componentPath.trim();
}

export { loadDynamicRoutes, resetDynamicRoutes };
export default router;
