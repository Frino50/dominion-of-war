import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import { localStore } from "./store/local.ts";

import Accueil from "@/views/Accueil.vue";
import Territory from "@/views/Territory.vue";

import Login from "@/views/Login.vue";
import Register from "@/views/Register.vue";

const routes: Array<RouteRecordRaw> = [
    {
        path: "/",
        name: "Accueil",
        component: Accueil,
        meta: { requiresAuth: true },
    },
    {
        path: "/territory",
        name: "Territory",
        component: Territory,
        meta: { requiresAuth: true },
    },
    {
        path: "/login",
        name: "Login",
        component: Login,
    },
    {
        path: "/register",
        name: "Register",
        component: Register,
    },
    {
        path: "/:pathMatch(.*)*",
        redirect: "/",
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach((to, _from, next) => {
    if (to.meta.requiresAuth && !localStore.pseudo) {
        next("/login");
    } else if (
        (to.path === "/login" || to.path === "/register") &&
        localStore.pseudo
    ) {
        next("/");
    } else {
        next();
    }
});

export default router;
